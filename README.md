JCommander is a minimal, cross-platform file manager inspired by the classic Norton Commander style.
It intentionally focuses on core file navigation and operations without attempting to replicate the feature breadth or complexity of modern, full-scale file managers.

The project is designed with a lean architecture and a clear separation between UI logic and platform-specific functionality. The goal is to provide a fast, predictable, and technically transparent commander-style file manager.

Platform Notes (Windows)

On Windows, JCommander uses a native DLL for certain file system operations.
If Windows Smart App Control / SmartScreen is enabled, this DLL may be blocked by the operating system. In that case, JCommander will automatically fall back to a pure Java implementation.

This fallback ensures that the application remains functional even when native integration is restricted, albeit with reduced native capabilities.

If Smart App Control is disabled or the DLL is explicitly allowed, the native path will be used automatically.

Project Status and Contributions

JCommander is intentionally minimal by design.
New ideas, feature requests, and architectural suggestions are explicitly welcome. The project is evolving, and feedback from users and developers is an important part of that process.

If you miss a feature or have an idea that fits the commander philosophy, feel free to open an issue or submit a proposal.

If you want, I can also:

adapt the tone to be more marketing-oriented or more technical,

shorten it to a very compact README,

or add a small “Philosophy” or “Non-Goals” section to make the minimalism explicit.
