# SHACL shape validator 

This action allows to validate RDF using SHACL shapes

### Configuration

This action must be provided with a JSON as configuration, specifying the following:
 - 'shape' must have as value either a URL that provides a SHACL shape or an RDF excerpt that is the shape. By default shapes are expected in TURTLE
 - 'shape-format' can be specified if the shape provided is not in TURTLE, possible values are turtle, ttl, json-ld, json-ld-11, rdf/xml, n-triples, nt, n3.
 - 'data-format' can be specified if the data provided is not in TURTLE, possible values are turtle, ttl, json-ld, json-ld-11, rdf/xml, n-triples, nt, n3.
 - 'output-format' can be specified to change the serialisation of the output report (by default in TURTLE), possible values are turtle, ttl, json-ld, json-ld-11, rdf/xml, n-triples, nt, n3.

### Register component

Use the following coordinates for import this action:

````json
{
    "source": "",
    "clazz": "helio.action.shacl.validator.ShaclValidator",
    "type": "ACTION"
}
