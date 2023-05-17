package searchengine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "index")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int pageId;

    private int lemmaId;

    @Column(columnDefinition = "FLOAT")
    private float rank;
}
