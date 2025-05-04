package backend.academy.scrapper.service.base.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.repository.jdbc.TagJdbcRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceJdbcImpl unit‑тесты")
class TagServiceJdbcImplTest {

    @Mock
    TagJdbcRepository repo;

    @InjectMocks
    TagServiceJdbcImpl service;

    @Nested
    @DisplayName("getOrCreateTagId()")
    class GetOrCreate {
        @Test
        @DisplayName("возвращает ID из репозитория")
        void returnsIdFromRepository() {
            // Arrange
            String name = "foo";
            long expected = 123L;
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
            String name = "bar";
            when(repo.insertOrGetIdByName(name)).thenThrow(new RuntimeException("db down"));

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getOrCreateTagId(name));
            assertThat(ex).hasMessageContaining("db down");
            verify(repo).insertOrGetIdByName(name);
        }
    }

    @Nested
    @DisplayName("getTagIdByName()")
    class GetByName {
        @Test
        @DisplayName("возвращает Optional с ID, если есть запись")
        void returnsPresentIfFound() {
            // Arrange
            String name = "tag";
            long id = 99L;
            when(repo.getIdByName(name)).thenReturn(Optional.of(id));

            // Act
            Optional<Long> result = service.getTagIdByName(name);

            // Assert
            assertThat(result).contains(id);
            verify(repo).getIdByName(name);
        }

        @Test
        @DisplayName("возвращает пустой Optional, если нет записи")
        void returnsEmptyIfNotFound() {
            // Arrange
            when(repo.getIdByName("missing")).thenReturn(Optional.empty());

            // Act
            Optional<Long> result = service.getTagIdByName("missing");

            // Assert
            assertThat(result).isEmpty();
            verify(repo).getIdByName("missing");
        }

        @Test
        @DisplayName("прокидывает исключение при ошибке репозитория")
        void propagatesException() {
            // Arrange
            when(repo.getIdByName("err")).thenThrow(new IllegalStateException("oops"));

            // Act & Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getTagIdByName("err"));
            assertThat(ex).hasMessageContaining("oops");
            verify(repo).getIdByName("err");
        }
    }
}
