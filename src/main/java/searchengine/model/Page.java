package searchengine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "page", indexes = @Index(columnList = "path"))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int siteId;

    @Column(columnDefinition = "VARCHAR(255)")
    private String path;

    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
}
