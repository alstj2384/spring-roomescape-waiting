package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "INVALID_NAME_LENGTH, 400",
            "INVALID_THEME_NAME_LENGTH, 400",
            "INVALID_THUMBNAIL_URL, 400",
            "THEME_NOT_FOUND, 404",
            "RESERVATION_NOT_FOUND, 404",
            "RESERVATION_TIME_NOT_FOUND, 404",
            "SLOT_NOT_FOUND, 404",
            "DUPLICATE_RESERVATION, 409",
            "RESERVATION_TIME_IN_USE, 409",
            "THEME_IN_USE, 409",
            "PAST_DATE_NOT_ALLOWED, 422",
            "PAST_RESERVATION_NOT_ALLOWED, 422",
            "INVALID_RANK_VALUE, 500"
    })
    void 도메인_오류를_HTTP_상태와_오류_응답으로_변환한다(ErrorCode errorCode, int expectedStatus) throws Exception {
        mockMvc.perform(get("/test/errors/{errorCode}", errorCode))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @RestController
    static class ExceptionTestController {
        @GetMapping("/test/errors/{errorCode}")
        public void throwException(@PathVariable("errorCode") ErrorCode errorCode) {
            throw new RoomEscapeException(errorCode);
        }
    }
}
