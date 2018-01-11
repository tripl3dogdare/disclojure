# Disclojure Contribution Guidelines

These are the basic guidelines for contributing your code to Disclojure. If you don't follow these guidelines, your contribution will likely be either rejected or you will be asked to revise it.

## Conduct and Attitude

- Follow the [Code of Conduct](CODE_OF_CONDUCT.md).
- Don't be afraid to work on smaller issues.
- Be willing to take criticism, feedback, and requests for changes.
- Be patient if your PRs/issues aren't dealt with immediately or accepted.

## Commit/PR Guidelines

- Use proper English to the best of your ability.
- Don't make someone dig through your code to figure out what you were trying to do. Just say it.
- Use descriptive commit messages.
  - Bad:
    - `committing this change lol`
    - `derp`
    - `f this bs`
  - Good:
    - `Update documentation`
    - `Fix issue #42`
    - `Add missing parameter to send-message`
- Keep commit messages under ~70 characters. Any additional info should go in the body.
- Keep your commit messages/notes concise but complete.
- Use lists and spacing to keep your commits readable.
- Avoid using a lot of non-standard abbreviations or acronyms.

## Project Guidelines

- Avoid adding new dependencies wherever possible.
- Avoid trying ridiculously, unnecessarily hard to avoid adding a new dependency.
- Make sure your contribution fits within the scope of the library. Disclojure is meant for interacting with Discord, it doesn't need Slack compatability, for example.

## Code Style

- If in doubt, look at the existing code for an example.
- If still in doubt, follow standard Clojure style.
- If *still* in doubt, keep things as readable as possible.

- Alignment, Indentation, and Spacing:
  - Use two-space indents and Unix-style newlines (\n, not \r\n).
  - Make sure there's a newline at the end of each file.
  - Prefer collapsing short lines onto the same line, within reason.
  - Prefer vertically compact code, within reason.
  - Prefer bindings or threading to deep nesting, within reason.
  - Leave space around complicated forms to aid in seeing where they begin/end.
  - Let bindings, maps, etc. should:
    - When there is only one binding/pair:
      - Start on the same line.
      - Have a space between the binding and the opening/closing braces.
    - When there is more than one binding/pair:
      - Start on the next line, including the opening brace.
      - The opening brace should be indented one level.
      - There should be a space between the opening brace and the first binding.
      - Further bindings should be aligned to the first binding.
      - There should be a space between the last binding and the closing brace.
    - When a binding/pair is too long/complex for one line:
      - Follow basic style rules for multiple bindings.
      - Start a new line and indent after the name/key, rather than in the middle of the value.
  - If a form has arguments spanning multiple lines, each argument should be on it's own line, rather than collapse short arguments onto the same line.

- Documentation:
  - Prefer docstrings to metadata where possible.
  - Multiline docstrings should be aligned to the first character, not the quote.
  - Double check your docstrings to make sure they come before any parameter lists, not after.
  - Use newlines to keep individual lines short.
  - If you use a newline in the middle of a sentence/paragraph, indent all inner lines by an extra space (one level from the initial quote).
  - Docstrings are formatted with Markdown.
    - Use code snippets, etc. as appropriate.
    - Newlines will be collapsed. If you intend to insert a newline rather than simply shortening the line, use a double newline.
  - Docstrings support linking to definitions as follows: `[[method-name]]` or `[[namespace/method-name]]`.
    - Link relevant definitions where possible in the normal flow of documentation.
    - Prefer qualified names for definitions in a different namespace.
    - Prefer unqualified names for structs, records, and definitions in the same namespace.
    - Use qualified names to disambiguate if necessary.
