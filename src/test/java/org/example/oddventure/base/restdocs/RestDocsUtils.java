package org.example.oddventure.base.restdocs;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;

public class RestDocsUtils {

    // 공통 성공 응답 필드 (data 제외)
    private static List<FieldDescriptor> commonSuccessResponseFields() {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(fieldWithPath("httpStatus").description("HTTP 상태 코드"));
        fields.add(fieldWithPath("code").description("응답 코드"));
        fields.add(fieldWithPath("success").description("성공 여부"));
        fields.add(fieldWithPath("message").description("응답 메시지"));
        fields.add(fieldWithPath("timestamp").description("응답 시간"));
        return fields;
    }

    // 공통 페이징 성공 응답 필드
    private static List<FieldDescriptor> commonPageResponseFields() {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(fieldWithPath("success").description("성공 여부"));
        fields.add(fieldWithPath("message").description("응답 메시지"));
        fields.add(fieldWithPath("timestamp").description("응답 시간"));
        return fields;
    }

    // 공통 에러 응답 필드
    private static List<FieldDescriptor> commonErrorResponseFields() {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(fieldWithPath("httpStatus").description("HTTP 상태 코드"));
        fields.add(fieldWithPath("code").description("응답 코드"));
        fields.add(fieldWithPath("success").description("성공 여부"));
        fields.add(fieldWithPath("message").description("오류 메시지"));
        fields.add(fieldWithPath("data").description("에러 응답의 경우 항상 null").optional());
        fields.add(fieldWithPath("timestamp").description("응답 시간"));
        fields.add(fieldWithPath("requestUrl").description("요청 URL"));
        return fields;
    }

    /**
     * 공통 성공 응답 필드 + {@code data} 내부의 필드를 병합한 {@code Snippet}을 생성
     *
     * <p>사용 예시</p>
     * <pre>{@code
     * .andDo(restDocs.document(
     *      requestFields(...),
     *      RestDocsUtils.successWithDataFields(
     *          fieldWithPath("data.userId").description("회원 ID"),
     *          fieldWithPath("data.username").description("회원 이름")
     *      )
     * ));
     * }</pre>
     */
    public static Snippet successWithDataFields(FieldDescriptor... dataFields) {
        List<FieldDescriptor> fields = commonSuccessResponseFields();

        if (dataFields == null || dataFields.length == 0) {
            fields.add(fieldWithPath("data").description("응답 데이터 (null일 수 있음)").optional());
        } else {
            fields.add(fieldWithPath("data").description("응답 데이터"));
            fields.addAll(List.of(dataFields));
        }

        return responseFields(fields);
    }

    /**
     * 공통 페이징 응답(ApiPageResponse) 필드 + {@code data.content} 배열 내부의 필드를 병합한 {@code Snippet}을 생성합니다.
     *
     * <p>사용 예시</p>
     * <pre>{@code
     * .andDo(restDocs.document(
     * queryParameters(
     * parameterWithName("page").description("페이지 번호"),
     * parameterWithName("size").description("페이지 크기")
     * ),
     * RestDocsUtils.pageResponseFields(
     * fieldWithPath("data.content[].userId").description("회원 ID"),
     * fieldWithPath("data.content[].username").description("회원 이름")
     * )
     * ));
     * }</pre>
     */
    public static Snippet pageResponseFields(FieldDescriptor... dataContentFields) {
        List<FieldDescriptor> fields = commonPageResponseFields();

        fields.add(fieldWithPath("data").description("페이징 응답 데이터"));
        fields.add(fieldWithPath("data.content").description("데이터 목록 배열"));

        if (dataContentFields != null && dataContentFields.length > 0) {
            fields.addAll(List.of(dataContentFields));
        }

        fields.add(fieldWithPath("data.totalElements").description("전체 항목 수"));
        fields.add(fieldWithPath("data.totalPages").description("전체 페이지 수"));
        fields.add(fieldWithPath("data.size").description("페이지 크기"));
        fields.add(fieldWithPath("data.number").description("현재 페이지 번호 (0부터 시작)"));

        return responseFields(fields);
    }

    /**
     * 공통 에러 응답 필드에 대한 {@code Snippet} 생성
     *
     * <p>사용 예시</p>
     * <pre>{@code
     * .andDo(restDocs.document(
     *     requestFields(...),
     *     RestDocsUtils.errorResponseFields()
     * ));
     * }</pre>
     */
    public static Snippet errorResponseFields() {
        return responseFields(commonErrorResponseFields());
    }
}