package apocrif.engine.jrules;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.org.apache.xpath.internal.operations.Variable;

public class Irl2RifTranslator
    implements
    IlrFactoryExplorer {

    PRHelper       factory = new PRHelper();

    // transient
    ProductionRule rifRule;

    Model          model;

    public Ruleset translate(IlrRuleset iRuleset) throws Exception {
        initModel( iRuleset );
        IlrRulesetFactory fRuleset = iRuleset.makeFactory();
        Ruleset rifRuleset = factory.createRuleset( fRuleset.getName() );

        IlrRuleFactory[] fRules = fRuleset.getRules();

        for ( IlrRuleFactory fRule : fRules ) {
            Rule rifRule = translateRule( fRule );
            rifRuleset.addRule( rifRule );
        }

        rifRule = null;
        return rifRuleset;
    }

    protected void initModel(IlrRuleset iRuleset) throws IlrXmlErrorException {
        IlrXmlDefaultDataDriver dataDriver = new IlrXmlDefaultDataDriver( iRuleset.getReflect() );
        model = new Model( iRuleset.getReflect(),
                           dataDriver.getXmlModel() );
    }

    protected AndCondition getCurrentCondition() {
        return (AndCondition) rifRule.getIfPart();
    }

    protected Rule translateRule(IlrRuleFactory fRule) {
        rifRule = factory.createProductionRule( fRule.getShortName() );
        rifRule.setIfPart( factory.createAndCondition() );

        for ( IlrCondition condition : fRule.getConditions() ) {
            condition.exploreCondition( this );
        }
        IlrStatement[] stmts = fRule.getStatements();
        if ( stmts.length != 1 ) throw new UnsupportedOperationException( "Body should have only one statement" );
        else {
            Uniterm rifStatement = (Uniterm) stmts[0].exploreStatement( this );
            rifRule.setThenPart( rifStatement );
        }

        return rifRule;
    }

    public Object exploreAssignable(IlrArrayElement assignable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreAssignable(IlrComponentPropertyValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreAssignable(IlrFieldValue assignable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreAssignable(IlrIndexedComponentPropertyValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreAssignable(IlrStaticFieldValue assignable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreAssignable(IlrVariable assignable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreCondition(IlrCollectCondition cond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreCondition(IlrEvaluateCondition cond) {
        // variable declaration
        exploreBindings( cond.getBindings() );

        // explore tests
        exploreTests( cond.getTests() );

        return null;
    }

    public Object exploreCondition(IlrExistsCondition cond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreCondition(IlrNotCondition cond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    protected void exploreBindings(IlrVariable[] bindings) {
        for ( IlrVariable fVariable : bindings ) {
            Term rifValue = (Term) fVariable.getValue().exploreValue( this );
            factory.declareVariable( rifRule,
                                     fVariable.getName(),
                                     rifValue );
        }
    }

    protected void exploreTests(IlrTest[] tests) {
        for ( IlrTest fTest : tests ) {
            Atomic rifTest = (Atomic) fTest.exploreTest( this );
            if ( rifTest != null ) getCurrentCondition().addCondition( rifTest );
        }
    }

    public Object exploreCondition(IlrSimpleCondition cond) {

        if ( cond.getObjectBinding() != null ) {
            IlrValue generator = cond.getEnumerator();
            QName xmlType = model.getXmlType( cond.getXOMClassScope() );
            if ( generator == null ) {

                factory.declareVariable( rifRule,
                                         cond.getObjectBinding().getName(),
                                         xmlType );
            } else if ( cond.getEnumeratorClause().equals( "from" ) ) {
                Term fromTerm = (Term) generator.exploreValue( this );
                factory.declareFromVariable( rifRule,
                                             cond.getObjectBinding().getName(),
                                             xmlType,
                                             fromTerm );
            } else {
                Term inTerm = (Term) generator.exploreValue( this );
                factory.declareInVariable( rifRule,
                                           cond.getObjectBinding().getName(),
                                           xmlType,
                                           inTerm );
            }
        }

        // variable declaration
        exploreBindings( cond.getBindings() );

        // explore tests
        exploreTests( cond.getTests() );

        return null;
    }

    public Object exploreCondition(IlrTimeCondition cond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrApplyAction action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrAssertAction action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrAssignment statement) {
        Term rightTerm = (Term) statement.getValue().exploreValue( this );
        if ( statement.getAssignable() instanceof IlrFieldValue ) {
            IlrFieldValue leftValue = (IlrFieldValue) statement.getAssignable();
            Term leftObject = (Term) leftValue.getObject().exploreValue( this );
            IlrReflectField field = leftValue.getReflectField();
            QName xmlField = model.getXmlField( field );
            QName xmlDeclaringCType = model.getXmlType( field.getDeclaringClass() );
            if ( xmlField == null || xmlDeclaringCType == null ) {
                throw new UnsupportedOperationException();
            }
            XmlFieldSetter fieldSetter = new XmlFieldSetter( xmlDeclaringCType,
                                                             xmlField,
                                                             leftObject,
                                                             rightTerm );
            return factory.createXmlFieldSetter( fieldSetter );

        }
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrBindStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrBreakStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrContinueStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrExecuteStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrForeachStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrForStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrFunctionInvocation statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrIfStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrMethodInvocation statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrModifyAction action) {
        Term target = (Term) action.getObject().exploreValue( this );
        Term statement = (Term) action.getStatements()[0].exploreStatement( this );
        Update modify = new Update( target,
                                    statement );
        return factory.createUpdateTerm( modify );
    }

    public Object exploreStatement(IlrRetractAction action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrReturnStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrStaticMethodInvocation statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskForkNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskGotoNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskIfNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskInstanceStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskJoinNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskSwitchNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTaskWhileNodeStatement node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrThrowStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTimeOutBlock statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrTryCatchFinallyStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrUnaryValue statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrUpdateAction action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreStatement(IlrWhileStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTask(IlrFlowTaskFactory task) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTask(IlrFunctionTaskFactory task) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTask(IlrRuleTaskFactory task) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrAfterTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrBeforeTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrBinaryTest test) {
        Term leftTerm = (Term) test.getFirstArgument().exploreValue( this );
        Term rightTerm = (Term) test.getSecondArgument().exploreValue( this );
        switch ( test.getKind() ) {
            case IlrBinaryTester.EQUAL : {
                return factory.createEqual( leftTerm,
                                            rightTerm );
            }
            case IlrBinaryTester.NOT_EQUAL :
                return factory.createNotEqualTerm( leftTerm,
                                                   rightTerm );
            case IlrBinaryTester.GREATER_THAN :
                return factory.createGreaterTerm( leftTerm,
                                                  rightTerm );
            case IlrBinaryTester.GREATER_OR_EQUAL :
                return factory.createGreaterEqualTerm( leftTerm,
                                                       rightTerm );
            case IlrBinaryTester.LESS_THAN :
                return factory.createLowerTerm( leftTerm,
                                                rightTerm );
            case IlrBinaryTester.LESS_OR_EQUAL :
                return factory.createLowerEqualTerm( leftTerm,
                                                     rightTerm );
            default :
                throw new UnsupportedOperationException();
        }
    }

    public Object exploreTest(IlrInstanceOfTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrNaryTest test) {
        if ( test.getKind() == IlrNaryTest.AND ) {
            exploreTests( test.getTests() );
            return null;
        }
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrNotTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrOccursinTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreTest(IlrUnaryTest test) {
        return test.getArgument().exploreValue( this );
    }

    public Object exploreTest(IlrUnknownTest test) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrArrayElement value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrArrayLength value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrAsValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrBinaryValue value) {
        Term leftTerm = (Term) value.getFirstArgument().exploreValue( this );
        Term rightTerm = (Term) value.getSecondArgument().exploreValue( this );
        switch ( value.getKind() ) {
            case IlrBinaryOperator.ADD :
                return factory.createPlusTerm( leftTerm,
                                               rightTerm );
            case IlrBinaryOperator.SUBTRACT :
                return factory.createMinusTerm( leftTerm,
                                                rightTerm );
            default :
                throw new UnsupportedOperationException();
        }
    }

    public Object exploreValue(IlrCastValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrClassTypeValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrCollectInSourceValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrComponentPropertyValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrConstantValue value) {
        return factory.createConst( value.getValue().toString(),
                                    model.getXmlType( value.getXOMType() ) );
    }

    public Object exploreValue(IlrContextValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrEventTimeValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrFieldValue value) {
        Term targetTerm = (Term) value.getObject().exploreValue( this );
        IlrReflectField field = value.getReflectField();
        QName xmlField = model.getXmlField( field );
        QName xmlDeclaringCType = model.getXmlType( field.getDeclaringClass() );
        if ( xmlField == null || xmlDeclaringCType == null ) {
            throw new UnsupportedOperationException();
        }
        XmlFieldGetter fieldGetter = new XmlFieldGetter( xmlDeclaringCType,
                                                         xmlField,
                                                         targetTerm );
        return factory.createXmlFieldGetter( fieldGetter );
    }

    public Object exploreValue(IlrFunctionInvocation value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrIndexedComponentPropertyValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrInstanceValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrIntervalValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    protected List<Term> exploreValues(IlrValue[] values) {
        List<Term> rifTerms = new ArrayList<Term>();
        for ( IlrValue value : values ) {
            Term rifValue = (Term) value.exploreValue( this );
            rifTerms.add( rifValue );
        }
        return rifTerms;
    }

    public Object exploreValue(IlrMethodInvocation value) {
        IlrReflectMethod method = value.getReflectMethod();
        if ( method.getName() == "equals" && method.getArgumentNumber() == 1 ) {
            Term leftTerm = (Term) value.getObject().exploreValue( this );
            Term rightTerm = (Term) value.getArguments()[0].exploreValue( this );
            return factory.createEqual( leftTerm,
                                        rightTerm );
        }
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrNewArrayInstanceValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrNewInstanceValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrObjectValue value) {
        IlrCondition cond = value.getCondition();
        if ( cond instanceof IlrSimpleCondition ) {
            IlrSimpleCondition sCond = (IlrSimpleCondition) cond;
            return getCurrentVariable( sCond.getObjectBinding().getName() );
        }
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrPropertyAccessValue arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrScopeValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrStaticFieldValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrStaticMethodInvocation value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrTestValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Object exploreValue(IlrUnaryValue value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    protected Variable getCurrentVariable(String name) {
        for ( Variable variable : rifRule.getVariables() ) {
            if ( name.equals( variable.getName() ) ) {
                return variable;
            }
        }
        return null;
    }

    public Object exploreValue(IlrVariable value) {
        return getCurrentVariable( value.getName() );
    }

}
