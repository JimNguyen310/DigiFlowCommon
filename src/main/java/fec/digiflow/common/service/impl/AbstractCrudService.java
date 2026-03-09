package fec.digiflow.common.service.impl;

import fec.digiflow.common.dto.LazyLoadEvent;
import fec.digiflow.common.dto.PagedResponse;
import fec.digiflow.common.exception.CommonException;
import fec.digiflow.common.message.GlobalMessage;
import fec.digiflow.common.service.CrudService;
import fec.digiflow.common.utils.CustomPageableUtils;
import fec.digiflow.common.utils.FilterSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.support.PageableUtils;

import java.util.List;

public abstract class AbstractCrudService<E, D, ID>  implements CrudService<E, D, ID> {

    protected abstract JpaRepository<E, ID> getRepository();
    protected abstract E toEntity(D dto);
    protected abstract D toDTO(E entity);
    protected abstract E toUpdateEntity(E entity, D dto);

    @Override
    public List<D> findAll() {
        return getRepository().findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public PagedResponse<D> paged (LazyLoadEvent lazyLoadEvent) {
        FilterSpecification<E> spec = new FilterSpecification<>(lazyLoadEvent);
        Pageable pageable = CustomPageableUtils.createPageable(lazyLoadEvent);
        JpaRepository<E, ID> repository = getRepository();
        if (!(repository instanceof JpaSpecificationExecutor))
            throw new CommonException(GlobalMessage.INTERNAL_SERVER_ERROR);
        return ((JpaSpecificationExecutor<E>) repository).findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public D findById(ID id) {
        return getRepository().findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CommonException(CommonMessage.NOT_FOUND));
    }

    @Override
    public D save(D dto) {
        T entity = toEntity(dto);
        return toDTO(getRepository().save(entity));
    }

    @Override
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    @Override
    public D update(ID id, D dto) {
        T entity = getRepository().findById(id)
                .orElseThrow(() -> new CommonException(CommonMessage.NOT_FOUND));
        return toDTO(getRepository().save(toUpdateEntity(entity, dto)));
    }
}
