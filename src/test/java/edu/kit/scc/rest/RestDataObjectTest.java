package edu.kit.scc.rest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.kit.scc.CdmiServerApplication;

import static org.snia.cdmiserver.Matchers.*;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CdmiServerApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestDataObjectTest {

	private HttpClient client = new DefaultHttpClient();
	private String url = "http://localhost:8080/";

	@Test
	public void A_Create_Object() throws Exception {
		HttpPut request = new HttpPut(url + "testfile");

		// add request header
		request.addHeader("Accept", "application/cdmi-object");
		request.addHeader("Content-Type", "application/cdmi-object");
		request.setEntity(new StringEntity(
				"{\"mimetype\": \"text/plain\",\"value\": \"This is a test\", \"metadata\":{created:by test}}\n",
				"UTF-8"));

		HttpResponse response = client.execute(request);

		assertThat(response.getStatusLine(), hasStatusCode(201));

		Header[] headers = response.getAllHeaders();
		assertThat(headers, hasHeader("Content-Type", "application/cdmi-object;charset=UTF-8"));

		HttpEntity entity = response.getEntity();
		String content = EntityUtils.toString(entity);
		assertTrue(content.contains("\"objectID\""));
		assertTrue(content.contains("\"parentID\""));
		assertTrue(content.contains("\"objectName\""));
		assertTrue(content.contains("\"capabilitiesURI\""));
		assertTrue(content.contains("\"objectType\":\"application/cdmi-object\""));
		assertTrue(content.contains("\"parentURI\":\"/\""));
		assertTrue(content.contains("\"completionStatus\":\"Complete\""));
		assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));
		assertTrue(content.contains("\"metadata\":{"));
	}

	@Test
	public void B_Get_Object() throws Exception {
		HttpGet request = new HttpGet(url + "testfile");

		// add request header
		request.addHeader("Accept", "application/cdmi-object");

		HttpResponse response = client.execute(request);

		assertThat(response.getStatusLine(), hasStatusCode(200));

		Header[] headers = response.getAllHeaders();
		assertThat(headers, hasHeader("Content-Type", "application/cdmi-object;charset=UTF-8"));

		HttpEntity entity = response.getEntity();
		String content = EntityUtils.toString(entity);
		assertTrue(content.contains("\"objectID\""));
		assertTrue(content.contains("\"parentID\""));
		assertTrue(content.contains("\"objectName\""));
		assertTrue(content.contains("\"capabilitiesURI\""));
		assertTrue(content.contains("\"objectType\":\"application/cdmi-object\""));
		assertTrue(content.contains("\"parentURI\":\"/\""));
		assertTrue(content.contains("\"completionStatus\":\"Complete\""));
		assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));
		assertTrue(content.contains("\"value\""));
		assertTrue(content.contains("\"metadata\":{"));

	}

	@Test
	public void B_Get_ObjectById() throws Exception {
		HttpGet request = new HttpGet(url + "cdmi_objectid/00000008001891C835303531613636662D366335642D3432");

		// add request header
		request.addHeader("Accept", "application/cdmi-object");

		HttpResponse response = client.execute(request);

		assertThat(response.getStatusLine(), hasStatusCode(200));

		Header[] headers = response.getAllHeaders();
		assertThat(headers, hasHeader("Content-Type", "application/cdmi-object;charset=UTF-8"));

		HttpEntity entity = response.getEntity();
		String content = EntityUtils.toString(entity);
		assertTrue(content.contains("\"objectID\""));
		assertTrue(content.contains("\"parentID\""));
		assertTrue(content.contains("\"objectName\""));
		assertTrue(content.contains("\"capabilitiesURI\""));
		assertTrue(content.contains("\"objectType\":\"application/cdmi-object\""));
		assertTrue(content.contains("\"parentURI\":\"/container1\""));
		assertTrue(content.contains("\"completionStatus\":\"Complete\""));
		assertTrue(content.contains("\"domainURI\":\"/cdmi_domain\""));
		assertTrue(content.contains("\"value\""));
		// assertTrue(content.contains("\"metadata\":{"));

	}

	@Test
	public void C_Delete_Object() throws Exception {
		HttpDelete request = new HttpDelete(url + "testfile");

		// add request header
		request.addHeader("Content-Type", "application/cdmi-object");

		HttpResponse response = client.execute(request);

		assertThat(response.getStatusLine(), hasStatusCode(204));
		Header[] headers = response.getAllHeaders();
		assertThat(headers, hasHeader("X-CDMI-Specification-Version", "1.1.1"));

	}

}
