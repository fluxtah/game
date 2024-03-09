find_path(CGLM_INCLUDE_DIR NAMES cglm/cglm.h HINTS /opt/homebrew/opt/cglm/include)
find_library(CGLM_LIBRARY NAMES cglm HINTS /opt/homebrew/opt/cglm/lib)

include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(cglm REQUIRED_VARS CGLM_LIBRARY CGLM_INCLUDE_DIR)
mark_as_advanced(CGLM_INCLUDE_DIR CGLM_LIBRARY)
