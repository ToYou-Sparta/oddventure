package org.example.oddventure.common.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@Getter
@JsonPropertyOrder({"success", "message", "data", "timestamp"})
public class ApiPageResponse<T> {

    private final boolean success;
    private final String message;
    private final PageData<T> data;
    private final LocalDateTime timestamp;

    private ApiPageResponse(PageData<T> data, String message) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공적인 요청에 대한 페이징 응답을 반환하는 메서드 주어진 데이터를 포함하여 HTTP 200 OK 상태 코드와 함께 응답을 반환
     *
     * @param page 요청 성공 시 반환할 페이징 데이터
     * @return HTTP 200 OK 응답과 함께 성공 데이터가 포함된 ApiPageResponse
     */
    public static <T> ResponseEntity<ApiPageResponse<T>> success(Page<T> page, String message) {
        return ResponseEntity.ok(new ApiPageResponse<>(PageData.from(page), message));
    }

    @Getter
    public static class PageData<T> {
        private final List<T> content;
        private final long totalElements;
        private final int totalPages;
        private final int size;
        private final int number;

        private PageData(List<T> content, long totalElements, int totalPages, int size, int number) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.size = size;
            this.number = number;
        }

        public static <T> PageData<T> from(Page<T> page) {
            return new PageData<>(
                    page.getContent(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getSize(),
                    page.getNumber()
            );
        }
    }
}