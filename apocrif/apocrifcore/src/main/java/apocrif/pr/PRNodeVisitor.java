package apocrif.pr;

import apocrif.core.NodeVisitor;

public interface PRNodeVisitor<T>
    extends
    NodeVisitor<T> {
    T visit(ProductionRule n);
}
