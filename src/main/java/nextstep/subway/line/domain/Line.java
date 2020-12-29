package nextstep.subway.line.domain;

import nextstep.subway.BaseEntity;
import nextstep.subway.line.domain.exceptions.ExploreSectionException;
import nextstep.subway.line.domain.exceptions.InvalidAddSectionException;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String color;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public Line() {
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Line(String name, String color, Station upStation, Station downStation, int distance) {
        this.name = name;
        this.color = color;
        sections.add(new Section(this, upStation, downStation, distance));
    }

    Line(String name, String color, List<Section> sections) {
        this.name = name;
        this.color = color;
        this.sections = sections;
    }

    public void update(Line line) {
        this.name = line.getName();
        this.color = line.getColor();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<Section> getSections() {
        return sections;
    }

    public Station findUpStation() {
        Section theFirstSection = findFirstSection();
        return theFirstSection.getUpStation();
    }

    public List<Station> getStations() {
        if (sections.isEmpty()) {
            return Arrays.asList();
        }

        List<Station> stations = new ArrayList<>();
        stations.add(this.findUpStation());

        Section currentSection = this.findFirstSection();

        while (currentSection != null) {
            stations.add(currentSection.getDownStation());
            currentSection = this.findNextSection(currentSection);
        }

        return stations;
    }

    public void addLineStation(Station upStation, Station downStation, int distance) {
        List<Station> stations = this.getStations();

        validateAddSection(stations, upStation, downStation);

        if (stations.isEmpty()) {
            this.getSections().add(new Section(this, upStation, downStation, distance));
            return;
        }

        if (isUpStationExisted(stations, upStation)) {
            this.getSections().stream()
                    .filter(it -> it.getUpStation() == upStation)
                    .findFirst()
                    .ifPresent(it -> it.updateUpStation(downStation, distance));

            this.getSections().add(new Section(this, upStation, downStation, distance));
        } else if (isDownStationExisted(stations, downStation)) {
            this.getSections().stream()
                    .filter(it -> it.getDownStation() == downStation)
                    .findFirst()
                    .ifPresent(it -> it.updateDownStation(upStation, distance));

            this.getSections().add(new Section(this, upStation, downStation, distance));
        } else {
            throw new RuntimeException();
        }
    }

    Section findNextSection(Section currentSection) {
        return this.sections.stream()
                .filter(it -> currentSection.getDownStation().equals(it.getUpStation()))
                .findFirst()
                .orElse(null);
    }

    private Section findFirstSection() {
        return this.sections.stream().filter(it -> it.isUpStationBelongsTo(findEndStationsInSections()))
                .findFirst()
                .orElseThrow(() -> new ExploreSectionException("해당 노선의 첫번째 구간을 찾을 수 없습니다."));
    }

    private List<Station> findEndStationsInSections() {
        List<Station> stations = sections.stream()
                .flatMap(it -> it.getStations().stream())
                .collect(Collectors.toList());

        return stations.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(it -> it.getValue() == 1L)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void validateAddSection(List<Station> stations, Station upStation, Station downStation) {
        validateIsAlreadyExist(stations, upStation, downStation);
        validateIsNotMatchAny(stations, upStation, downStation);
    }

    private void validateIsAlreadyExist(List<Station> stations, Station upStation, Station downStation) {
        if (isUpStationExisted(stations, upStation) && isDownStationExisted(stations, downStation)) {
            throw new InvalidAddSectionException("이미 등록된 구간 입니다.");
        }
    }

    private void validateIsNotMatchAny(List<Station> stations, Station upStation, Station downStation) {
        if (!stations.isEmpty() && stations.stream().noneMatch(it -> it == upStation) &&
                stations.stream().noneMatch(it -> it == downStation)) {
            throw new InvalidAddSectionException("등록할 수 없는 구간 입니다.");
        }
    }

    private boolean isUpStationExisted(List<Station> stations, Station upStation) {
        return stations.stream().anyMatch(it -> it == upStation);
    }

    private boolean isDownStationExisted(List<Station> stations, Station downStation) {
        return stations.stream().anyMatch(it -> it == downStation);
    }
}
