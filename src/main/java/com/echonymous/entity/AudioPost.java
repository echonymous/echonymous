package com.echonymous.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("AUDIO")
public class AudioPost extends Post{
    private String filePath;
}
