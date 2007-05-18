package apocrif.core;

public interface NodeVisitor<T> {
    T visit(AndCondition n);

    T visit(Const n);

    T visit(Equal n);

    T visit(Uniterm n);

    T visit(Variable n);

    T visit(LogicalRule n);

    T visit(Implies n);
}
