package fec.digiflow.common.service;

import fec.digiflow.common.dto.PagedResponse;
import fec.digiflow.common.service.BaseMapper;
import fec.digiflow.common.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseServiceImpl<E, D, ID> implements BaseService<D, ID> {

    protected final JpaRepository<E, ID> repository;
    protected final BaseMapper<E, D> mapper;

    protected BaseServiceImpl(JpaRepository<E, ID> repository, BaseMapper<E, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public D save(D dto) {
        E entity = mapper.toEntity(dto);
        E savedEntity = repository.save(entity);
        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public D update(ID id, D dto) {
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID not found: " + id));

        mapper.updateEntityFromDto(dto, existingEntity);

        E updatedEntity = repository.save(existingEntity);
        return mapper.toDto(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public D findById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID not found: " + id));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<D> findAll(Pageable pageable) {
        Page<E> entityPage = repository.findAll(pageable);
        List<D> dtoContent = mapper.toDtoList(entityPage.getContent());

        return new PagedResponse<>(
                dtoContent,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isLast(),
                entityPage.isFirst()
        );
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ID not found: " + id);
        }
        repository.deleteById(id);
    }
}