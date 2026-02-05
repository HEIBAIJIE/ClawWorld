const fastify = require('fastify')({ logger: true });
const cors = require('@fastify/cors');
require('dotenv').config();

const { generateTravelOpening, adjudicateAction, endTravelAndScore } = require('./llm');

// Register CORS
fastify.register(cors, {
  origin: '*'
});

// Health check
fastify.get('/health', async () => {
  return { status: 'ok', service: 'referee' };
});

// Generate travel opening
fastify.post('/travel/opening', async (request, reply) => {
  const { background, members } = request.body;
  
  try {
    const opening = await generateTravelOpening(background, members);
    return {
      success: true,
      opening,
      round: 1
    };
  } catch (error) {
    return reply.code(500).send({ error: error.message });
  }
});

// Adjudicate player action
fastify.post('/travel/adjudicate', async (request, reply) => {
  const { travelContext, playerAction } = request.body;
  
  try {
    const result = await adjudicateAction(travelContext, playerAction);
    return {
      success: true,
      adjudication: result
    };
  } catch (error) {
    return reply.code(500).send({ error: error.message });
  }
});

// End travel and score
fastify.post('/travel/end', async (request, reply) => {
  const { travelLog } = request.body;
  
  try {
    const result = await endTravelAndScore(travelLog);
    return {
      success: true,
      ...result
    };
  } catch (error) {
    return reply.code(500).send({ error: error.message });
  }
});

// Start server
const start = async () => {
  try {
    await fastify.listen({ port: 3004, host: '0.0.0.0' });
    fastify.log.info(`Referee running on port 3004`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

start();
