#!/usr/bin/env bash
set -euo pipefail

# ===== Oddventure ECS Fargate 배포 스크립트 =====
# 사용법: ./scripts/ecs-deploy.sh
# 필수 환경변수: AWS_REGION, ACCOUNT_ID, ECR_REPO, ECS_CLUSTER, ECS_SERVICE, IMAGE_TAG

# ===== 필수 환경변수 점검 =====
: "${AWS_REGION:?AWS_REGION required}"
: "${ACCOUNT_ID:?ACCOUNT_ID required}"
: "${ECR_REPO:?ECR_REPO required}"
: "${ECS_CLUSTER:?ECS_CLUSTER required}"
: "${ECS_SERVICE:?ECS_SERVICE required}"
: "${IMAGE_TAG:?IMAGE_TAG required}"

# ===== 변수 설정 =====
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
FULL_IMAGE_URI="${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
TASK_DEFINITION_FAMILY="oddventure-task"
CONTAINER_NAME="app"

echo "============================================"
echo "🚀 Oddventure ECS Fargate 배포 시작"
echo "============================================"
echo "[INFO] AWS Region: ${AWS_REGION}"
echo "[INFO] ECR Registry: ${ECR_REGISTRY}"
echo "[INFO] Image: ${FULL_IMAGE_URI}"
echo "[INFO] ECS Cluster: ${ECS_CLUSTER}"
echo "[INFO] ECS Service: ${ECS_SERVICE}"
echo "============================================"

# ===== Step 1: ECR 로그인 =====
echo "[Step 1/5] ECR 로그인 중..."
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_REGISTRY}"
echo "✅ ECR 로그인 완료"

# ===== Step 2: Docker 이미지 빌드 및 푸시 =====
echo "[Step 2/5] Docker 이미지 빌드 중..."
docker build -t "${ECR_REPO}:${IMAGE_TAG}" .
docker tag "${ECR_REPO}:${IMAGE_TAG}" "${FULL_IMAGE_URI}"
echo "✅ 이미지 빌드 완료"

echo "[Step 3/5] ECR로 이미지 푸시 중..."
docker push "${FULL_IMAGE_URI}"
echo "✅ 이미지 푸시 완료"

# ===== Step 4: Task Definition 업데이트 =====
echo "[Step 4/5] Task Definition 업데이트 중..."

# 현재 Task Definition 가져오기
CURRENT_TASK_DEF=$(aws ecs describe-task-definition \
  --task-definition "${TASK_DEFINITION_FAMILY}" \
  --query 'taskDefinition' \
  --output json)

# 새 이미지로 Task Definition 업데이트
NEW_TASK_DEF=$(echo "${CURRENT_TASK_DEF}" | jq --arg IMAGE "${FULL_IMAGE_URI}" '
  .containerDefinitions[0].image = $IMAGE |
  del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy)
')

# 새 Task Definition 등록
NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json "${NEW_TASK_DEF}" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "✅ 새 Task Definition 등록 완료: ${NEW_TASK_DEF_ARN}"

# ===== Step 5: ECS 서비스 업데이트 =====
echo "[Step 5/5] ECS 서비스 업데이트 중..."
aws ecs update-service \
  --cluster "${ECS_CLUSTER}" \
  --service "${ECS_SERVICE}" \
  --task-definition "${NEW_TASK_DEF_ARN}" \
  --force-new-deployment \
  --output json > /dev/null

echo "✅ ECS 서비스 업데이트 요청 완료"

# ===== 배포 상태 모니터링 =====
echo ""
echo "============================================"
echo "📊 배포 상태 모니터링 중..."
echo "============================================"

MAX_WAIT=300  # 최대 5분 대기
WAIT_INTERVAL=10
ELAPSED=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
  SERVICE_STATUS=$(aws ecs describe-services \
    --cluster "${ECS_CLUSTER}" \
    --services "${ECS_SERVICE}" \
    --query 'services[0].deployments' \
    --output json)
  
  PRIMARY_COUNT=$(echo "${SERVICE_STATUS}" | jq '[.[] | select(.status == "PRIMARY")] | length')
  ACTIVE_COUNT=$(echo "${SERVICE_STATUS}" | jq '[.[] | select(.status == "ACTIVE")] | length')
  
  RUNNING=$(echo "${SERVICE_STATUS}" | jq '[.[] | select(.status == "PRIMARY")][0].runningCount')
  DESIRED=$(echo "${SERVICE_STATUS}" | jq '[.[] | select(.status == "PRIMARY")][0].desiredCount')
  
  echo "[${ELAPSED}s] Running: ${RUNNING}/${DESIRED}, Active deployments: ${ACTIVE_COUNT}"
  
  # PRIMARY만 남고 원하는 개수만큼 실행 중이면 완료
  if [ "${ACTIVE_COUNT}" -eq 0 ] && [ "${RUNNING}" -eq "${DESIRED}" ]; then
    echo ""
    echo "============================================"
    echo "🎉 배포 완료!"
    echo "============================================"
    echo "Cluster: ${ECS_CLUSTER}"
    echo "Service: ${ECS_SERVICE}"
    echo "Image: ${FULL_IMAGE_URI}"
    echo "Running Tasks: ${RUNNING}/${DESIRED}"
    exit 0
  fi
  
  sleep $WAIT_INTERVAL
  ELAPSED=$((ELAPSED + WAIT_INTERVAL))
done

echo ""
echo "⚠️ 배포 타임아웃 (${MAX_WAIT}초)"
echo "배포가 아직 진행 중일 수 있습니다. AWS 콘솔에서 확인해주세요."
exit 1

