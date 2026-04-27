package com.automobile.ecom.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "subcategory", writeTypeHint = WriteTypeHint.FALSE)
//@Document(indexName = "subcategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryDocument {

    @Id
    private String id;

    private String name;
    private String photoUrl;
    private Boolean isActive;

    private String categoryId;
    private String categoryName;
}