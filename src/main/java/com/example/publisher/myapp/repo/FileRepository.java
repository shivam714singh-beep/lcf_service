        package com.example.publisher.myapp.repo;

        import com.example.publisher.myapp.entity.FileEntity;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;

        @Repository
        public interface FileRepository extends JpaRepository<FileEntity, Long> {}
