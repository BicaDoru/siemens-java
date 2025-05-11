package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    // swapped BAD_REQUEST with CREATED when the item was created
    // modified createdItem to put the errors in a map and returns them in the response

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    // modified NO_CONTENT to NOT_FOUND when the item is not found

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // modified the ACCEPTED to NOT_FOUND when the item is not found
    // modified the CREATED to OK when the item is updated

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // check if id is missing

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // async processes items and maps result or exception to the appropriate http response

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        return future.handle((items, ex) -> {
            if (ex == null) {
                return ResponseEntity.ok(items);
            }
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof CancellationException) {
                return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
            } else if (cause instanceof ConstraintViolationException) {
                return ResponseEntity.badRequest().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        });
    }


}
