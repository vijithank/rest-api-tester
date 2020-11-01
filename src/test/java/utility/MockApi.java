package utility;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class MockApi {
    private static final int PORT = 2020;

    public static WireMockServer wireMockServer = new WireMockServer(PORT);


    public MockApi() {
        wireMockServer.start();
        WireMock.configureFor("localhost", PORT);
    }

    public void stubFailedGet() {
        WireMock.stubFor(WireMock.get("/non-existing-api")
                .willReturn(aResponse()
                        .withStatus(400)));
    }

    public static void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.shutdownServer();
        }
    }
}
