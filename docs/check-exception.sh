#!/bin/bash
set -euo pipefail

echo "Checking duplicate task exception classes after package move..."
fd -t f -a 'Task(NotFound|AccessDenied|Business|Validation)Exception.java'

echo
echo "Show package declarations for any duplicates:"
rg -n -C1 -P 'package\s+com\.omori\.taskmanagement\.springboot\.exceptions(\.task)?;' \
  src/main/java/com/omori/taskmanagement/springboot/exceptions -g '!**/target/**'

echo
echo "Check imports across codebase to ensure only the new package is referenced:"
rg -n -C1 -P 'com\.omori\.taskmanagement\.springboot\.exceptions\.task\.(Task(NotFound|AccessDenied|Business|Validation)Exception)'