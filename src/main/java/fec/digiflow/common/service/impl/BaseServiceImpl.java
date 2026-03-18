package fec.digiflow.common.service.impl;

import fec.digiflow.common.dto.PagedResponse;
import fec.digiflow.common.service.BaseMapper;
import fec.digiflow.common.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class BaseServiceImpl<E, D, ID> implements BaseService<D, ID> {

    protected final JpaRepository<E, ID> repository;

    protected final BaseMapper<E, D> mapper;

    protected BaseServiceImpl(JpaRepository<E, ID> repository, BaseMapper<E, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public D save(D dto) {
        E entity = mapper.toEntity(dto);
        E savedEntity = repository.save(entity);
        return mapper.toDto(savedEntity);
    }

    @Override
    public D update(ID id, D dto) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ID not found: " + id);
        }
        // Có thể custom logic merge entity ở đây tùy dự án
        E entity = mapper.toEntity(dto);
        E updatedEntity = repository.save(entity);
        return mapper.toDto(updatedEntity);
    }

    @Override
    public D findById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID not found: " + id));
        return mapper.toDto(entity);
    }

    @Override
    public List<D> findAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public PagedResponse<D> findAll(Pageable pageable) {
        Page<D> page = repository.findAll(pageable).map(mapper::toDto);
        return PagedResponse.from(page);
    }

    @Override
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ID not found: " + id);
        }
        repository.deleteById(id);
    }
}