#include <GLFW/glfw3.h>
#include <printf.h>
#include "joystick.h"

CJoyAxes getJoyAxes(int joy) {
    CJoyAxes joyAxes = {NULL, 0};
    int axesCount;
    joyAxes.axes = glfwGetJoystickAxes(joy, &axesCount);
    joyAxes.count = axesCount;

    return joyAxes;
}

CJoyButtons getJoyButtons(int joy) {
    CJoyButtons joyButtons = {NULL, 0};
    int buttonsCount;
    joyButtons.buttons = glfwGetJoystickButtons(joy, &buttonsCount);
    joyButtons.count = buttonsCount;
    return joyButtons;
}
