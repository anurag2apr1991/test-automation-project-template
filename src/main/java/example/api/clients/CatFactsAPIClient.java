package example.api.clients;

import com.ta.core.api.BaseAPIClient;
import io.restassured.RestAssured;

public class CatFactsAPIClient extends BaseAPIClient<CatFactsAPIClient> {

    private static final String BASE_PATH = "/facts";

    public CatFactsAPIClient getRandomFact() {
        setResponse(RestAssured.get(BASE_PATH));
        return this;
    }

}
