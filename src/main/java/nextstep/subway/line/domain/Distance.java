package nextstep.subway.line.domain;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Distance {
    private final long distance;

    protected Distance() {
        distance = 0;
    }

    public Distance(long distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance 는 음수를 가질수 없다.");
        }
        this.distance = distance;
    }

    public Distance plus(final Distance distance) {
        return distance.plus(this.distance);
    }

    public Distance minus(final Distance source) {
        return source.minusBy(distance);
    }

    private Distance plus(final long source) {
        return new Distance(source + distance);
    }

    private Distance minusBy(final long source) {
        return new Distance(source - distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Distance distance1 = (Distance) o;
        return distance == distance1.distance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance);
    }
}
