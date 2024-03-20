#ifndef GAME_BULLETDEBUGDRAWER_H
#define GAME_BULLETDEBUGDRAWER_H


#include <LinearMath/btIDebugDraw.h>
#include "../../../include/vertex.h"
#include <vector>

class BulletDebugDrawer : public btIDebugDraw {
private:
    int debugMode;
    std::vector<DebugVertex> debugVertices;
    int vertexCount = 0;

public:
    BulletDebugDrawer() : debugMode(DBG_DrawWireframe) {}

    int getDebugMode() const override;
    void setDebugMode(int mode) override;
    void drawLine(const btVector3 &from, const btVector3 &to, const btVector3 &color) override;
    void drawContactPoint(const btVector3 &PointOnB, const btVector3 &normalOnB, btScalar distance, int lifeTime, const btVector3 &color) override;
    void reportErrorWarning(const char *warningString) override;
    void draw3dText(const btVector3 &location, const char *textString) override;

    void clearLines() override;

    void *getVertexData();

    int getVertexCount() const;
};


#endif //GAME_BULLETDEBUGDRAWER_H
