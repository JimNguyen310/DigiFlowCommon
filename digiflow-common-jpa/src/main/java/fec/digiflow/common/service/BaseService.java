package fec.digiflow.common.service;

import fec.digiflow.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BaseService<D, ID> {

    D save(D dto);

    D update(ID id, D dto);

    D findById(ID id);

    List<D> findAll();

    PagedResponse<D> findAll(Pageable pageable);

    void deleteById(ID id);
}
