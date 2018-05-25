package io.github.jlameira.amazonconfig;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IAmazonService {
    void initial();

    void store(MultipartFile file, String path);

    Resource load(String filename, String local_path);

    String path(String filename, String local_path);
}
