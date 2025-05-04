package backend.academy.scrapper.service.base.impl.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.model.db.tag.Tag;
import backend.academy.scrapper.repository.jpa.tag.TagJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceJpaImpl unit‑тесты")
class TagServiceJpaImplTest {

    @Mock
    TagJpaRepository repo;

    @InjectMocks
    TagServiceJpaImpl service;

    @Nested
    @DisplayName("getOrCreateTagId()")
    class GetOrCreate {
        @Test
        @DisplayName("возвращает ID из репозитория")
        void returnsIdFromRepository() {
            // Arrange
            String name = "xyz";
            long expected = 77L;
            when(repo.insertOrGetIdByName(name)).thenReturn(expected);

            // Act
            long actual = service.getOrCreateTagId(name);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(repo).insertOrGetIdByName(name);
        }

        @Test
        @DisplayName("прокидывает исключение при ошибке репозитория")
        void propagatesException() {
            // Arrange
            when(repo.insertOrGetIdByName("bad")).thenThrow(new RuntimeException("fail"));

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getOrCreateTagId("bad"));
            assertThat(ex).hasMessageContaining("fail");
            verify(repo).insertOrGetIdByName("bad");
        }
    }

    @Nested
    @DisplayName("getTagIdByName()")
    class GetByName {
        @Test
        @DisplayName("преобразует Optional<Tag> → Optional<ID> при наличии")
        void returnsMappedIdIfFound() {
            // Arrange
            String name = "aaa";
            Tag tag = new Tag(314L, name);
            when(repo.findByName(name)).thenReturn(Optional.of(tag));

            // Act
            Optional<Long> result = service.getTagIdByName(name);

            // Assert
            assertThat(result).contains(314L);
            verify(repo).findByName(name);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если Tag отсутствует")
        void returnsEmptyIfNotFound() {
            // Arrange
            when(repo.findByName("none")).thenReturn(Optional.empty());

            // Act
            Optional<Long> result = service.getTagIdByName("none");

            // Assert
            assertThat(result).isEmpty();
            verify(repo).findByName("none");
        }

        @Test
        @DisplayName("прокидывает исключение при ошибке репозитория")
        void propagatesException() {
            // Arrange
            when(repo.findByName("err")).thenThrow(new IllegalStateException("db error"));

            // Act & Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getTagIdByName("err"));
            assertThat(ex).hasMessageContaining("db error");
            verify(repo).findByName("err");
        }
    }
}
