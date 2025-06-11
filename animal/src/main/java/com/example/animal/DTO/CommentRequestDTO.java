// File: src/main/java/com/example/animal/DTO/CommentRequestDTO.java
package com.example.animal.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {
    private Long postId;
    private String cmContent;
}