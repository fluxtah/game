#include "include/aabb.h"

bool aabbCollision(AABB *a, AABB *b) {
    if (a->min[0] > b->max[0] || a->max[0] < b->min[0]) return false;
    if (a->min[1] > b->max[1] || a->max[1] < b->min[1]) return false;
    if (a->min[2] > b->max[2] || a->max[2] < b->min[2]) return false;
    return true;
}

