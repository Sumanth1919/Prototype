import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Prototype {
    private static final WireMockServer wireMockServer = new WireMockServer(options().port(9081));
    public static String ExcelExtraction(String Listmethod) throws FilloException{
        List<String> listurl=new ArrayList<String>();
        List<String> listresponsheader=new ArrayList<String>();
        List<String> listmethod=new ArrayList<String>();
        List<String> liststatus=new ArrayList<String>();
        Fillo fillo=new Fillo();
        Connection connection=fillo.getConnection("D:\\news.xlsx");
        String strQuery="Select * from Sheet1";
        Recordset recordset=connection.executeQuery(strQuery);
        while(recordset.next()){

            listurl.add(recordset.getField("URL"));
            listresponsheader.add(recordset.getField("RESPONSE BODY"));
            listmethod.add(recordset.getField("METHOD"));
            liststatus.add(recordset.getField("STATUS"));


        }
        if(Listmethod.equalsIgnoreCase("URL")) {
            return listurl.get(0);
        }
        else if (Listmethod.equalsIgnoreCase("RESPONSE")) {
            return listresponsheader.get(0);

        }
        return listmethod.get(0);

    }
    public static void main(String[] args) throws IOException, FilloException {

        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        URLMatching("/baeldung/.*",200, "application/json");
        String Resp = CreateHttpClient("baeldung/wiremock");
        System.out.println(Resp);

    }

    private static void URLMatching(String URI, int ResponseCode, String values) {
        wireMockServer.stubFor(get(urlPathMatching(URI)).willReturn(aResponse().withStatus(ResponseCode)
                .withHeader("Content-Type", values).withBody("\"testing-library\": \"WireMock\"")));
    }

    private static String CreateHttpClient(String URL) throws IOException, FilloException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:9081/" + URL);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        verify(getRequestedFor(urlEqualTo(ExcelExtraction("URL"))));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals("application/json", httpResponse.getFirstHeader("Content-Type").getValue());
        assertEquals(ExcelExtraction("RESPONSE"), stringResponse);
        return stringResponse;
    }

    private static String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        return convertInputStreamToString(inputStream);
    }

    private static String convertInputStreamToString(InputStream inputStream) {

        Scanner scanner = new Scanner(inputStream, String.valueOf(StandardCharsets.UTF_8));
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }

}
