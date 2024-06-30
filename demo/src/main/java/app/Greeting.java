package app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder @With
public class Greeting {
    String greeting;
    String target;
    LocalTime time;
}
