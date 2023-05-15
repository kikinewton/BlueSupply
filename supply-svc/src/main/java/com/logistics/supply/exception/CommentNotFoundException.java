package com.logistics.supply.exception;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(String commentSource) {
        super("Comment from %s not found".formatted(commentSource));
    }
}
