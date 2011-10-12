package be.docarch.accessibility.ooo.rdf;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.BlankNode;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.rdf.Literal;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.IssueEvent;
import be.docarch.accessibility.IssueListener;
import be.docarch.accessibility.Element;
import be.docarch.accessibility.Checker;
import be.docarch.accessibility.ooo.URIs;

/**
 *
 * @author Bert Frees
 */
public class Assertions extends RDFClass {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private final XNamedGraph graph;
    private final Assertors assertors;
    private final TestSubjects testSubjects;
    private final TestCases testCases;

    public Assertions(XNamedGraph graph) {
        this.graph = graph;
        assertors = new Assertors(graph);
        testSubjects = new TestSubjects(graph);
        testCases = new TestCases(graph);
    }

    public Assertion create(Issue issue) {
        return new Assertion(issue);
    }

    public Assertion read(XResource assertion,
                          Provider<Checker> checkers)
                   throws Exception {

        XResource testresult = null;
        Checker checker = null;
        Check check = null;
        Element element = null;
        Date checkDate = null;
        int count = 1;

        try {

            if (!graph.getStatements(assertion, URIs.RDF_TYPE, URIs.EARL_ASSERTION).hasMoreElements()) { throw new Exception("not of type EARL_ASSERTION"); }

            XEnumeration assertorEnum = graph.getStatements(assertion, URIs.EARL_ASSERTEDBY, null);
            XEnumeration testSubjectEnum = graph.getStatements(assertion, URIs.EARL_SUBJECT, null);
            XEnumeration testCaseEnum = graph.getStatements(assertion, URIs.EARL_TEST, null);
            XEnumeration testResultEnum = graph.getStatements(assertion, URIs.EARL_RESULT, null);
            if (!assertorEnum.hasMoreElements())    { throw new Exception("no EARL_ASSERTEDBY statement"); }
            if (!testSubjectEnum.hasMoreElements()) { throw new Exception("no EARL_SUBJECT statement"); }
            if (!testCaseEnum.hasMoreElements())    { throw new Exception("no EARL_TEST statement"); }
            if (!testResultEnum.hasMoreElements())  { throw new Exception("no EARL_RESULT statement"); }
            XURI assertor = URI.create(xContext, ((Statement)assertorEnum.nextElement()).Object.getStringValue());
            checker = assertors.read(assertor, checkers).getChecker();
            XURI testcase = URI.create(xContext, ((Statement)testCaseEnum.nextElement()).Object.getStringValue());
            check = testCases.read(testcase, checker).getCheck();
            XResource testsubject = BlankNode.create(xContext, ((Statement)testSubjectEnum.nextElement()).Object.getStringValue());
            element = testSubjects.read(testsubject).getElement();
            testresult = BlankNode.create(xContext, ((Statement)testResultEnum.nextElement()).Object.getStringValue());
            if (!graph.getStatements(testresult, URIs.EARL_OUTCOME, null).hasMoreElements())             { throw new Exception("no EARL_OUTCOME statement"); }
            if (!graph.getStatements(testresult, URIs.RDF_TYPE, URIs.EARL_TESTRESULT).hasMoreElements()) { throw new Exception("not of type EARL_TESTRESULT"); }
            XEnumeration timestamps = graph.getStatements(testresult, URIs.DCT_DATE, null);
            if (!timestamps.hasMoreElements()) { throw new Exception("no DCT_DATE statement"); }
            checkDate = dateFormat.parse(((Statement)timestamps.nextElement()).Object.getStringValue());
            XEnumeration counts = graph.getStatements(testresult, URIs.A11Y_COUNT, null);
            if (counts.hasMoreElements()) {
                count = Integer.parseInt(((Statement)counts.nextElement()).Object.getStringValue());
            }
            Issue issue = new Issue(element, check, checker, checkDate, count);
            XEnumeration ignore = graph.getStatements(testresult, URIs.A11Y_IGNORE, null);
            if (ignore.hasMoreElements()) {
                if (((Statement)ignore.nextElement()).Object.getStringValue().equals("true")) {
                    issue.ignored(true);
                }
            }
            if (graph.getStatements(testresult, URIs.EARL_OUTCOME, URIs.EARL_PASSED).hasMoreElements()) {
                issue.repaired(true);
            }
            return new Assertion(issue, assertion, testresult);

        } catch (Exception e) {
            String message = "Invalid assertion" +
                    "\nChecker: " + ((checker==null) ? "?" : checker.getIdentifier()) +
                    "\nCheck: "   + ((check==null)   ? "?" : check.getIdentifier()) +
                    "\nElement: " + ((element==null) ? "?" : element.toString()) +
                    "\nException: " + e.getMessage();
            try {
                graph.removeStatements(assertion, null, null);
                if (testresult != null) {
                    graph.removeStatements(testresult, null, null);
                }
                message += "\nAssertion removed from report.";
            } catch (Exception ee) {
                message += "\nCould not remove assertion from report.";
            }

            throw new Exception(message);
        }
    }

    public class Assertion implements IssueListener {

        private final Issue issue;
        private XResource assertion;
        private XResource testresult;

        private Assertion(Issue issue) {
            this.issue = issue;
            issue.addListener(this);
        }

        private Assertion(Issue issue,
                          XResource assertion,
                          XResource testresult) {

            this(issue);
            this.assertion = assertion;
            this.testresult = testresult;
        }

        public Issue getIssue() {
            return issue;
        }

        public XResource write() throws Exception {

            if (assertion == null) {
                assertion = xRepository.createBlankNode();
                testresult = xRepository.createBlankNode();

                XURI testcase = testCases.create(issue.getCheck()).write();
                XURI assertor = assertors.create(issue.getChecker()).write();
                XResource subject = testSubjects.create(issue.getElement()).write();

                graph.addStatement(testresult, URIs.RDF_TYPE, URIs.EARL_TESTRESULT);
                graph.addStatement(testresult, URIs.EARL_OUTCOME, URIs.EARL_FAILED);
                graph.addStatement(testresult, URIs.DCT_DATE, Literal.create(xContext, dateFormat.format(issue.getCheckDate())));
                graph.addStatement(assertion, URIs.RDF_TYPE, URIs.EARL_ASSERTION);
                graph.addStatement(assertion, URIs.EARL_RESULT, testresult);
                graph.addStatement(assertion, URIs.EARL_TEST, testcase);
                graph.addStatement(assertion, URIs.EARL_SUBJECT, subject);
                graph.addStatement(assertion, URIs.EARL_ASSERTEDBY, assertor);
                if (issue.getCount()>1) {
                    graph.addStatement(testresult, URIs.A11Y_COUNT, Literal.create(xContext, String.valueOf(issue.getCount())));
                }
            }
            return assertion;
        }

        public void issueUpdated(IssueEvent event) {

            if (event.getSource() != issue) { return; }

            try {
                if (assertion == null) { write(); }
                switch (event.type) {
                    case IGNORE:
                        graph.removeStatements(testresult, URIs.A11Y_IGNORE, null);
                        if (issue.ignored()) {
                            graph.addStatement(testresult, URIs.A11Y_IGNORE, Literal.create(xContext, "true"));
                        }
                        document.setModified();
                        break;
                    case REPAIR:
                        graph.removeStatements(testresult, URIs.EARL_OUTCOME, null);
                        if (issue.repaired()) {
                            graph.addStatement(testresult, URIs.EARL_OUTCOME, URIs.EARL_PASSED);
                        } else {
                            graph.addStatement(testresult, URIs.EARL_OUTCOME, URIs.EARL_FAILED);
                        }
                        document.setModified();
                        break;
                    case REMOVE:
                        graph.removeStatements(assertion, null, null);
                        document.setModified();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
