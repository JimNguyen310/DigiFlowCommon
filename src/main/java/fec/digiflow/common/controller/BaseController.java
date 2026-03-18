package fec.digiflow.common.controller;

import fec.digiflow.common.dto.BaseResponse;
import fec.digiflow.common.dto.PagedResponse;
import fec.digiflow.common.service.BaseService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<D, ID> {

    protected final BaseService<D, ID> service;

    protected BaseController(BaseService<D, ID> service) {
        this.service = service;
    }

    @PostMapping
    public BaseResponse<D> create(@RequestBody D dto) {
        return BaseResponse.success(service.save(dto));
    }

    @PutMapping("/{id}")
    public BaseResponse<D> update(@PathVariable ID id, @RequestBody D dto) {
        return BaseResponse.success(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public BaseResponse<D> getById(@PathVariable ID id) {
        return BaseResponse.success(service.findById(id));
    }

    @GetMapping("/all")
    public BaseResponse<List<D>> getAll() {
        return BaseResponse.success(service.findAll());
    }

    @GetMapping
    public BaseResponse<PagedResponse<D>> getAllPaginated(Pageable pageable) {
        return BaseResponse.success(service.findAll(pageable));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable ID id) {
        service.deleteById(id);
        return BaseResponse.success(null);
    }
}
