package apocrif.core;

public interface Node {
    <T> T accept(NodeVisitor<T> explorer);
}
