package be.repository;

import be.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findAllBy();

    @Query("""
            select f from File f
            left join fetch f.tags
                where :category member of f.categories
            """)
    List<File> findByCategory(Category category);

    @Query("""
            select f from File f
            left join fetch f.tags
                where f.fileType = :fileType
            """)
    List<File> findByFileType(FileType fileType);

    @Query("""
            select f from File f
            left join fetch f.tags
                where :category member of f.categories
                and f.fileType = :fileType
            """)
    List<File> findByCategoryAndFileType(Category category, FileType fileType);
}
