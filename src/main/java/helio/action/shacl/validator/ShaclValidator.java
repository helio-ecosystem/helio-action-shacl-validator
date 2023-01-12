package helio.action.shacl.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ShaclPlainValidator;
import com.google.gson.JsonObject;

import helio.blueprints.Action;
import helio.blueprints.exceptions.ActionException;

/**
 * This action allows to validate RDF using SHACL shapes. Either the data and the shapes can be provided as URL or excepts of RDF in different formats. In addition, the output report serialization can be choose. All serializations are by default expected to be TURTLE.
 * @author Andrea Cimmino
 *
 */
public class ShaclValidator implements Action {

	private Shapes shape;
	private Lang dataFormat = Lang.TURTLE;
	private Lang outputFormat = Lang.TURTLE;
	private static final String SHAPE_TOKEN = "shape";
	private static final String SHAPE_FORMAT_TOKEN = "shape-format";
	private static final String DATA_FORMAT_TOKEN = "data-format";
	private static final String OUTPUT_FORMAT_TOKEN = "output-format";
	private static final String NT_TOKEN = "nt";
	private static final String TTL_TOKEN = "ttl";

	
	public void configure(JsonObject configuration) {
		Graph shapesGraph = null;
		Lang format = Lang.TURTLE;
		if(configuration==null)
			throw new IllegalArgumentException("Provide a valid configuration containing a json key 'shape' which value can be a URL that provides a SHACL shape or an RDF excerpt that is the shape. Optionaly, they key 'format' can be used to specify the serialisation of the shape.");
		if (configuration.has(SHAPE_TOKEN)) {
			if (configuration.has(SHAPE_FORMAT_TOKEN))
				format = parseFormat(configuration.get(SHAPE_FORMAT_TOKEN).getAsString());

			String rawShape = configuration.get(SHAPE_TOKEN).getAsString();
			if (!isURI(rawShape)) {
				try {
					shapesGraph = parseRDF(rawShape, format.getName());
				} catch (Exception e) {
					throw new IllegalArgumentException(e.getMessage());
				}
			} else {
				shapesGraph = RDFDataMgr.loadGraph(rawShape, format);
			}
		} else {
			throw new IllegalArgumentException(
					"Provided configuration must contain a json key 'shape' which value can be a URL that provides a SHACL shape or an RDF excerpt that is the shape. Optionaly, they key 'format' can be used to specify the serialisation of the shape.");
		}
		if(configuration.has(DATA_FORMAT_TOKEN))
			dataFormat = parseFormat(configuration.get(DATA_FORMAT_TOKEN).getAsString());
		if(configuration.has(OUTPUT_FORMAT_TOKEN))
			outputFormat = parseFormat(configuration.get(OUTPUT_FORMAT_TOKEN).getAsString());
		shape = Shapes.parse(shapesGraph);
	}



	public String run(String values) throws ActionException {
		Graph dataGraph = null;
		if (isURI(values)) {
			dataGraph = RDFDataMgr.loadGraph(values, dataFormat);
		} else {
			try {
				dataGraph = parseRDF(values, dataFormat.getName());
			} catch (Exception e) {
				throw new ActionException(e.getMessage());
			}
		}

		ShaclPlainValidator validator = new ShaclPlainValidator();
		ValidationReport report = validator.validate(shape, dataGraph);
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
		RDFDataMgr.write(reportStream, report.getModel(), outputFormat);
		return new String(reportStream.toByteArray());
	}

	private boolean isURI(String values) {
		try {
			URI.create(values);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Graph parseRDF(String data, String format) throws ActionException {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new ByteArrayInputStream(data.getBytes()), null, format);
		} catch (Exception e) {
			throw new ActionException(e.getMessage());
		}
		return model.getGraph();
	}
	
	private Lang parseFormat(String format) {
		if(Lang.TURTLE.getName().equalsIgnoreCase(format))
			return Lang.TURTLE;
		if(Lang.JSONLD.getName().equalsIgnoreCase(format))
			return Lang.JSONLD;
		if(Lang.JSONLD11.getName().equalsIgnoreCase(format))
			return Lang.JSONLD11;
		if(Lang.N3.getName().equalsIgnoreCase(format))
			return Lang.N3;
		if(Lang.NTRIPLES.getName().equalsIgnoreCase(format))
			return Lang.NTRIPLES;
		if(format.equalsIgnoreCase(NT_TOKEN))
			return Lang.NT;
		if(format.equalsIgnoreCase(TTL_TOKEN))
			return Lang.TTL;
		if(Lang.RDFXML.getName().equalsIgnoreCase(format))
			return Lang.RDFXML;
		throw new IllegalArgumentException("Provided format is not supported, choose one from (case insensitive): turtle, ttl, json-ld, json-ld-11, n3, N-Triples, rdf/xml");
	}

	// Getters & Setters


	public Shapes getShape() {
		return shape;
	}




	

	
}
