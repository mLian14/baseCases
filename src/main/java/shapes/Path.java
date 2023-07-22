package shapes;

import java.util.Objects;

/**
 * @auther lianmeng
 * @create 20.07.23
 */
public class Path {
    public Point startPoint, endPoint;

    public Path(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(startPoint, path.startPoint) && Objects.equals(endPoint, path.endPoint);
    }

}
