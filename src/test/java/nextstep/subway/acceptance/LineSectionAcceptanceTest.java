package nextstep.subway.acceptance;

import static nextstep.subway.utils.RestAssuredCRUD.응답결과가_BAD_REQUEST;
import static nextstep.subway.utils.RestAssuredCRUD.응답결과가_OK;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.applicaion.dto.StationRequest;
import nextstep.subway.applicaion.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("지하철 구간 관련 기능")
public class LineSectionAcceptanceTest extends AcceptanceTest{

    private LineResponse 일호선;
    private StationResponse 일호선역1;
    private StationResponse 일호선역2;
    private StationResponse 일호선역3;
    private StationResponse 일호선역4;

    @BeforeEach
    public void setUp() {
        super.setUp();

        일호선역1 = StationAcceptanceTest.지하철_역_을_등록한다(StationRequest.of("1호선역1")).as(StationResponse.class);
        일호선역2 = StationAcceptanceTest.지하철_역_을_등록한다(StationRequest.of("1호선역2")).as(StationResponse.class);
        일호선역3 = StationAcceptanceTest.지하철_역_을_등록한다(StationRequest.of("1호선역3")).as(StationResponse.class);
        일호선역4 = StationAcceptanceTest.지하철_역_을_등록한다(StationRequest.of("1호선역4")).as(StationResponse.class);

        LineRequest lineRequest = LineRequest.of("신분당선", "bg-red-600", 일호선역1.getId(), 일호선역4.getId(), 10);
        일호선 = LineAcceptanceTest.지하철_노선을_등록한다(lineRequest).as(LineResponse.class);
    }

    @DisplayName("지하철 노선에 구간을 추가한 후 구간을 조회해서 확인한다.")
    @Test
    void 지하철_노선에_구간을_등록하고_조회해서_확인한다() {
        // when // then
        ExtractableResponse<Response> 노선구간등록결과;
        노선구간등록결과 = 지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역2, 10);
        응답결과가_OK(노선구간등록결과);
        노선구간등록결과 = 지하철_노선에_구간을_등록한다(일호선, 일호선역2, 일호선역4, 10);
        응답결과가_OK(노선구간등록결과);
        노선구간등록결과 = 지하철_노선에_구간을_등록한다(일호선, 일호선역4, 일호선역3, 10);
        응답결과가_OK(노선구간등록결과);

        // when
        ExtractableResponse<Response> 노선역조회결과 = 지하철_노선의_역들을_조회한다(일호선);
        // then
        원하는_역들이_들어있다(노선역조회결과, Arrays.asList(일호선역1, 일호선역2));
    }

    @DisplayName("지하철 노선에 추가하는 새로운 구간의 상행역이 기존 하행역과 같이 않으면 등록 할 수 없다.")
    @Test
    void 지하철_노선에_구간을_등록_실패한다_1() {
        // given
        지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역2, 10);
        지하철_노선에_구간을_등록한다(일호선, 일호선역2, 일호선역3, 10);

        // when // then
        ExtractableResponse<Response> 구간등록결과 = 지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역4, 5);
        응답결과가_BAD_REQUEST(구간등록결과);
    }

    @DisplayName("지하철 노선에 추가하는 새로운 구간의 하행역이 현재 이미 등록되어 있는 역이면 등록 할 수 없다.")
    @Test
    void 지하철_노선에_구간을_등록_실패한다_2() {
        // given
        지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역2, 10);
        지하철_노선에_구간을_등록한다(일호선, 일호선역2, 일호선역3, 10);

        // when // then
        ExtractableResponse<Response> 구간등록결과 = 지하철_노선에_구간을_등록한다(일호선, 일호선역3, 일호선역1, 5);
        응답결과가_BAD_REQUEST(구간등록결과);
    }

    @DisplayName("지하철 노선에서 특정 역을 삭제 성공한 후 조회해서 확인한다.")
    @Test
    void 지하철_노선에서_역을_삭제_성공한다() {
        // given
        지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역3, 10);
        지하철_노선에_구간을_등록한다(일호선, 일호선역3, 일호선역4, 10);

        // when // then
        ExtractableResponse<Response> 구간삭제결과 = 지하철_노선에서_역을_삭제한다(일호선.getId(), 일호선역4.getId());
        응답결과가_OK(구간삭제결과);

        // when
        ExtractableResponse<Response> 노선역조회결과 = 지하철_노선의_역들을_조회한다(일호선);
        // then
        원하는_역들이_들어있다(노선역조회결과, Arrays.asList(일호선역1, 일호선역3));
    }

    @DisplayName("지하철 노선에서 역이 최하행역이 아니라서 삭제 불가하다.")
    @Test
    void 지하철_노선에서_역을_삭제_실패한다_1() {
        // given
        지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역2, 10);
        지하철_노선에_구간을_등록한다(일호선, 일호선역2, 일호선역3, 10);

        // when // then
        ExtractableResponse<Response> 구간삭제결과 = 지하철_노선에서_역을_삭제한다(일호선.getId(), 일호선역2.getId());
        응답결과가_BAD_REQUEST(구간삭제결과);
    }

    @DisplayName("지하철 노선에서 구간이 1개라서 역을 삭제 할 수 없다.")
    @Test
    void 지하철_노선에서_역을_삭제_실패한다_2() {
        // given
        지하철_노선에_구간을_등록한다(일호선, 일호선역1, 일호선역2, 10);

        // when // then
        ExtractableResponse<Response> 구간삭제결과 = 지하철_노선에서_역을_삭제한다(일호선.getId(), 일호선역2.getId());
        응답결과가_BAD_REQUEST(구간삭제결과);
    }

    public static void 원하는_역들이_들어있다(ExtractableResponse<Response> 노선역조회결과, List<StationResponse> 원하는결과) {
        응답결과가_OK(노선역조회결과);

        LineResponse lineResponse = 노선역조회결과.as(LineResponse.class);
        List<String> resultNames = lineResponse.getStations().stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());

        List<String> names = 원하는결과.stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());

        assertThat(resultNames).containsAll(names);
    }

    private static ExtractableResponse<Response> 지하철_노선의_역들을_조회한다(LineResponse 일호선역) {
        return RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/lines/{lineId}", 일호선역.getId())
            .then().log().all()
            .extract();
    }

    public static ExtractableResponse<Response> 지하철_노선에_구간을_등록한다(LineResponse line, StationResponse upStation, StationResponse downStation, int distance) {
        SectionRequest sectionRequest = SectionRequest.of(upStation.getId(), downStation.getId(), distance);

        return RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(sectionRequest)
            .when().post("/lines/{lineId}/sections", line.getId())
            .then().log().all()
            .extract();
    }

    public static ExtractableResponse<Response> 지하철_노선에서_역을_삭제한다(Long lineId, Long stationId) {
        return RestAssured
            .given().log().all()
            .when().delete("/lines/{lineId}/sections?stationId={stationId}", lineId, stationId)
            .then().log().all()
            .extract();
    }

}
