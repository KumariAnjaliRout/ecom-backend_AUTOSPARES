package com.automobile.ecom.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "category", writeTypeHint = WriteTypeHint.FALSE)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDocument {

    @Id
    private String id;

    private String name;
    private String photoUrl;
    private Boolean isActive;
}