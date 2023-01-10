package tests;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shacl.Shapes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import helio.action.shacl.validator.ShaclValidator;
import helio.blueprints.exceptions.ActionException;

public class ShaclValidatorTest {

	// SET TRUE FOR FOLLOWING TRACES
	private boolean showErrorMessages = true;

	// Configuration tests
	private ShaclValidator validator = new ShaclValidator();	
	private String shapeStrNT = "<https://astrea.linkeddata.es/shapes#1277b387effe1ea8b7cf6171d6155a1b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/shacl#NodeShape> .\n"
			+ "<https://astrea.linkeddata.es/shapes#1277b387effe1ea8b7cf6171d6155a1b> <http://www.w3.org/ns/shacl#nodeKind> <http://www.w3.org/ns/shacl#IRI> .\n"
			+ "<https://astrea.linkeddata.es/shapes#1277b387effe1ea8b7cf6171d6155a1b> <http://www.w3.org/ns/shacl#property> <https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> .\n"
			+ "<https://astrea.linkeddata.es/shapes#1277b387effe1ea8b7cf6171d6155a1b> <http://www.w3.org/ns/shacl#targetClass> <http://www.w3.org/2006/time#Instant> .\n"
			+ "<https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/shacl#PropertyShape> .\n"
			+ "<https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> <http://www.w3.org/ns/shacl#datatype> <http://www.w3.org/2001/XMLSchema#dateTime> .\n"
			+ "<https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> <http://www.w3.org/ns/shacl#nodeKind> <http://www.w3.org/ns/shacl#Literal> .\n"
			+ "<https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> <http://www.w3.org/ns/shacl#path> <http://www.w3.org/2006/time#inXSDDateTime> .\n"
			+ "<https://astrea.linkeddata.es/shapes#54d31883388335bc143215bf49c965ea> <http://www.w3.org/ns/shacl#pattern> \"-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\\\\\\\.[0-9]+)?|(24:00:00(\\\\\\\\.0+)?))(Z|(\\\\\\\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?\" .\n";
	private String wrongDataTTL = "@prefix ex: <http://example.org/ns#> .\n"
			+ "@prefix time: <http://www.w3.org/2006/time#> .\n"
			+ "\n"
			+ "ex:Now a time:Instant ;\n"
			+ "    time:inXSDDateTime \"Robert\" .\n";
	private String correctDataTTL = "@prefix ex: <http://example.org/ns#> .\n"
			+ "@prefix time: <http://www.w3.org/2006/time#> .\n"
			+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
			+ "ex:Now a time:Instant ;\n"
			+ "    time:inXSDDateTime \"1994-11-05T13:15:30Z\"^^xsd:dateTime .\n";
	
	private Model shape = ModelFactory.createDefaultModel();
	private Model correctData = ModelFactory.createDefaultModel();
	
	@Before
	public void loadModel() {
		shape.read(new ByteArrayInputStream(shapeStrNT.getBytes()), null, "n3");
		correctData.read(new ByteArrayInputStream(correctDataTTL.getBytes()), null, "turtle");
	}
	
	private String shapeTo(String format) {
		Writer w = new StringWriter();
		shape.write(w, format);
		return w.toString();
		
	}
	
	private String dataTo(String format) {
		Writer w = new StringWriter();
		correctData.write(w, format);
		return w.toString();
		
	}
	
	private Graph parseRDF(String data, String format) throws ActionException {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new ByteArrayInputStream(data.getBytes()), null, format);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ActionException(e.getMessage());
			
		}
		return model.getGraph();
	}
	
	@Test
	public void testEmptyConfiguration() {
		JsonObject config = new JsonObject();
		boolean thrown = false;
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testShapeSyntaxError() {
		JsonObject config = new JsonObject();
		config.addProperty("shape", "");
		boolean thrown = false;
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	
	
	@Test
	public void testShapeURL() {
		JsonObject config = new JsonObject();
		String url = "https://raw.githubusercontent.com/helio-ecosystem/helio-ecosystem/main/resources/siotrx/actions/shacl/lcc-shapes.ttl";
		config.addProperty("shape", url);
		Shapes expectedShape = Shapes.parse(url);
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeURLFormatJsonLD() {
		JsonObject config = new JsonObject();
		String url = "https://raw.githubusercontent.com/helio-ecosystem/helio-ecosystem/main/resources/siotrx/actions/shacl/lcc-shapes.jsonld";
		config.addProperty("shape", url);
		config.addProperty("shape-format", "json-ld");
		Shapes expectedShape = Shapes.parse(url);
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	
	
	@Test
	public void testShapeContent() throws ActionException {
		JsonObject config = new JsonObject();
		String format = "turtle";
		String rawShape = shapeTo(format);
		config.addProperty("shape", rawShape);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentSyntaxError() throws ActionException {
		JsonObject config = new JsonObject();
		config.addProperty("shape", "");
		boolean thrown = false;	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true;
		}
		
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testShapeContentFormatError() throws ActionException {
		JsonObject config = new JsonObject();
		
		config.addProperty("shape", shapeTo("turtle"));
		config.addProperty("shape-format", "xml/rdf");
		
		boolean thrown = false;	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true;
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testShapeContentFormatTURTLE() throws ActionException {
		JsonObject config = new JsonObject();
		String format = "turtle";
		String rawShape = shapeTo(format);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);

		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentFormatTTL() throws ActionException {
		JsonObject config = new JsonObject();
		String format = "ttl";
		String rawShape = shapeTo(format);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);

		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentFormatNT() throws ActionException {
		JsonObject config = new JsonObject();
		String format = "nt";
		String rawShape = shapeTo(format);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);

		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentFormatNTriples() throws ActionException {
		JsonObject config = new JsonObject();
		
		String format = "n-triples";
		String rawShape = shapeTo(format);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentFormatN3() throws ActionException {
		JsonObject config = new JsonObject();
		
		String format = "n3";
		String rawShape = shapeTo(format);
		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));

	}
	
	@Test
	public void testShapeContentFormatXML() throws ActionException {
		JsonObject config = new JsonObject();
		
		String format = "rdf/xml";
		String rawShape = shapeTo(format);

		Shapes expectedShape = Shapes.parse(parseRDF(rawShape, format));
		
		config.addProperty("shape", rawShape);
		config.addProperty("shape-format", format);
	
		try {
			validator.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(validator.getShape().getGraph().isIsomorphicWith(expectedShape.getGraph()));
		Assert.assertTrue(expectedShape.getGraph().isIsomorphicWith(validator.getShape().getGraph()));
	}
	
	
	// Run tests
	
	@Test
	public void testShapeRunNotConforms() throws ActionException {
		String report = "";
		JsonObject config = new JsonObject();
		config.addProperty("shape", shapeTo("turtle"));

		try {
			validator.configure(config);
			report = validator.run(wrongDataTTL);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		
		Assert.assertTrue(report.contains("sh:conforms  false"));
	}
	
	@Test
	public void testShapeRunConforms() throws ActionException {
		String report = "";
		JsonObject config = new JsonObject();
		config.addProperty("shape", shapeTo("turtle"));

		try {
			validator.configure(config);
			report = validator.run(correctDataTTL);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		Assert.assertTrue(report.contains("sh:conforms  true"));
	}
	
	@Test
	public void testShapeRunConformsFormatJsonLD() throws ActionException {
		String report = "";
		JsonObject config = new JsonObject();
		config.addProperty("shape", shapeTo("turtle"));
		config.addProperty("data-format", "json-ld");
		try {
			validator.configure(config);
			report = validator.run(dataTo("json-ld"));
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		Assert.assertTrue(report.contains("sh:conforms  true"));
	}
	
	@Test
	public void testShapeRunConformsOutputFormatJsonLD() throws ActionException {
		String report = "";
		JsonObject config = new JsonObject();
		config.addProperty("shape", shapeTo("turtle"));
		config.addProperty("output-format", "json-ld");
		try {
			validator.configure(config);
			report = validator.run(correctDataTTL);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		JsonObject reportJson = (new Gson()).fromJson(report, JsonObject.class);
		Assert.assertTrue(reportJson.get("sh:conforms").getAsJsonObject().get("@value").getAsBoolean());
	}
	
	@Test
	public void testShapeRunConformsWrongFormat() throws ActionException {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		config.addProperty("shape", shapeTo("turtle"));
		config.addProperty("data-format", "json-ld");
		try {
			validator.configure(config);
			validator.run(dataTo("turtle"));
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true;
		}
		Assert.assertTrue(thrown);
	}
	
	
	// TEST IF DATA FORMAT IS DIFFERENT FROM ACTUAL DATA FORMAT + IDEM SHAPE
}
