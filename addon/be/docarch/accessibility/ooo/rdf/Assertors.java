package be.docarch.accessibility.ooo.rdf;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;

import be.docarch.accessibility.Checker;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.ooo.URIs;

/**
 *
 * @author Bert Frees
 */
public class Assertors extends RDFClass {

    private final XNamedGraph graph;

    private Map<String,Assertor> xURIMap = new TreeMap<String,Assertor>();
    private Map<Checker,Assertor> checkerMap = new HashMap<Checker,Assertor>();

    public Assertors(XNamedGraph graph) {        
        this.graph = graph;
    }

    public Assertor create(Checker checker) {
        Assertor a = checkerMap.get(checker);
        if (a == null) {
            a = new Assertor(checker);
            checkerMap.put(checker, a);
        }
        return a;
    }

    public Assertor read(XURI assertor,
                         Provider<Checker> checkers)
                  throws Exception {

        Assertor a = xURIMap.get(assertor.getStringValue());
        if (a == null) {
            if (graph.getStatements(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR).hasMoreElements()) {
                Checker checker = checkers.get(assertor.getStringValue());
                if (checker != null) {
                    a = new Assertor(checker, assertor);
                }
            }
        }
        if (a == null) { throw new Exception(); }
        xURIMap.put(assertor.getStringValue(), a);
        return a;
    }

    public class Assertor {

        private final Checker checker;
        private XURI assertor;

        private Assertor(Checker checker) {
            this.checker = checker;
        }

        private Assertor(Checker checker,
                         XURI assertor) {

            this(checker);
            this.assertor = assertor;
        }

        public Checker getChecker() {
            return checker;
        }

        public XURI write() throws Exception {

            if (assertor == null) {
                assertor = URI.create(xContext, checker.getIdentifier());
                graph.addStatement(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR);
              //graph.addStatement(URIs.A11Y_CHECKER, URIs.RDFS_SUBCLASSOF, URIs.EARL_ASSERTOR); // in plaats van vorige ?
                graph.addStatement(assertor, URIs.RDF_TYPE, URIs.A11Y_CHECKER);
              //graph.addStatement(assertor, URIs.DCT_DATE, Literal.create(xContext, dateFormat.format(lastChecked))); // terug invoeren !
            }
            return assertor;
        }
    }
}
