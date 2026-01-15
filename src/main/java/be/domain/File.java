package be.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseTimeEntity implements EntityId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileOverview;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "file_category",
            joinColumns = @JoinColumn(name = "file_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private List<Category> categories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String fileMediaType;

    @Column(updatable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String savedFileName;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "file_tag_join",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @Builder
    public File(
            String fileOverview, List<Category> categories, FileType fileType,
            String originalFileName, String savedFileName, String fileMediaType
    ) {
        this.fileOverview = fileOverview;
        this.categories = categories;
        this.fileType = fileType;
        this.originalFileName = originalFileName;
        this.savedFileName = savedFileName;
        this.fileMediaType = fileMediaType;
    }

    public MediaType getFileMediaType() {
        return MediaType.parseMediaType(this.fileMediaType);
    }

    public void enrichMetadata(String fileOverview, List<Category> category, List<Tag> tags) {
        this.fileOverview = fileOverview;
        this.categories = category;
        this.tags = tags;
    }

}
