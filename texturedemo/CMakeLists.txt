cmake_minimum_required(VERSION 3.4.1)

IF (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Windows")
    ADD_DEFINITIONS(-DWindows)
ELSE (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Linux")
    ADD_DEFINITIONS(-DLinux)
ENDIF ()

include_directories( ${CMAKE_SOURCE_DIR}/src/main/cpp/inc )

aux_source_directory( src/main/cpp/src SRC_LIST )

add_library( native-renderer
             SHARED
             ${SRC_LIST} )

target_link_libraries( native-renderer
                       # for 'AAssetManager_fromJava'
                       android
                       # for opengl es 3.0 library
                       GLESv3
                       # for log library
                       log )