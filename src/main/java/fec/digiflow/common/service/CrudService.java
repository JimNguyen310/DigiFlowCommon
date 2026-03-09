package fec.digiflow.common.service;

import fec.digiflow.common.dto.LazyLoadEvent;
import fec.digiflow.common.dto.PagedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CrudService<E, D, ID> {

    List<D> findAll();

    PagedResponse<D> paged (LazyLoadEvent lazyLoadEvent);

    D findById(ID id);

    D save(D d);

    D update(ID id, D d);

    void deleteById(ID id);
}
