---
name: Use Optional in Java
description: Guidelines for using java.util.Optional to handle nullability and improve code safety.
---

# Java Optional Best Practices

In this project, we prioritize the use of `java.util.Optional` for handling potentially null values. This improves code readability and prevents `NullPointerException`.

## Guidelines

1. **Return Optional instead of null**: When a method might not return a value, use `Optional<T>` as the return type.
   ```java
   public Optional<User> findUser(String id) {
       // ...
   }
   ```

2. **Prefer Optional over null checks**: Use fluent methods like `.map()`, `.flatMap()`, and `.orElse()` instead of explicit `if (x != null)`.
   ```java
   // Good
   String name = findUser(id)
       .map(User::getName)
       .orElse("Anonymous");

   // Avoid
   User user = findUser(id);
   String name = (user != null) ? user.getName() : "Anonymous";
   ```

3. **Do not use Optional for fields or parameters**: Optional is primarily intended for return types. For parameters and fields, use standard nullability annotations or patterns.

4. **Avoid .get()**: Always use safe alternatives like `.orElse()`, `.orElseGet()`, or `.ifPresent()`. If you must use it, verify with `.isPresent()` first, though this is often redundant if you use the fluent API.
