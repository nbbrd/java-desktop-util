package ec.util.demo.ext;

@lombok.Value
public class DefaultGridCell<R, C> {

    R rowValue;

    C columnValue;
}
