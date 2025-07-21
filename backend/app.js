const express = require('express');
const axios = require('axios');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Free Hugging Face API configuration
const HF_API_TOKEN = process.env.HF_API_TOKEN || 'your-huggingface-token-here';
const HF_MODEL_URL = 'https://api-inference.huggingface.co/models/microsoft/DialoGPT-medium';

// Free search API configuration (using DuckDuckGo Instant Answer API - completely free)
const SEARCH_API_URL = 'https://api.duckduckgo.com/';

// AI Agent class for handling text enhancement
class AIAgent {
  constructor() {
    this.systemPrompt = `You are a helpful AI assistant that enhances notes and provides useful information. 
    Your responses should be concise, practical, and actionable.`;
  }

  // Enhance text using Hugging Face free API
  async enhanceText(text) {
    try {
      const response = await axios.post(
        HF_MODEL_URL,
        {
          inputs: `Enhance this note with helpful details and subtasks: "${text}"`,
          parameters: {
            max_length: 200,
            temperature: 0.7,
            do_sample: true
          }
        },
        {
          headers: {
            'Authorization': `Bearer ${HF_API_TOKEN}`,
            'Content-Type': 'application/json'
          }
        }
      );

      if (response.data && response.data[0] && response.data[0].generated_text) {
        return response.data[0].generated_text;
      } else {
        return this.fallbackEnhancement(text);
      }
    } catch (error) {
      console.error('Hugging Face API error:', error.message);
      return this.fallbackEnhancement(text);
    }
  }

  // Fallback enhancement when API fails
  fallbackEnhancement(text) {
    const keywords = text.toLowerCase();
    let enhancement = `Enhanced Note: ${text}\n\n`;
    
    // Simple rule-based enhancement
    if (keywords.includes('meeting') || keywords.includes('call')) {
      enhancement += "Subtasks:\n- Prepare agenda\n- Set up meeting link\n- Send calendar invite\n- Follow up after meeting";
    } else if (keywords.includes('travel') || keywords.includes('flight') || keywords.includes('trip')) {
      enhancement += "Subtasks:\n- Check passport validity\n- Book accommodation\n- Plan itinerary\n- Check weather forecast\n- Pack essentials";
    } else if (keywords.includes('study') || keywords.includes('learn') || keywords.includes('research')) {
      enhancement += "Subtasks:\n- Gather resources\n- Create study schedule\n- Take notes\n- Practice/Review\n- Test knowledge";
    } else if (keywords.includes('project') || keywords.includes('work')) {
      enhancement += "Subtasks:\n- Define requirements\n- Create timeline\n- Assign responsibilities\n- Track progress\n- Review and iterate";
    } else {
      enhancement += "Subtasks:\n- Break down into smaller steps\n- Set deadlines\n- Gather required resources\n- Monitor progress\n- Complete and review";
    }
    
    return enhancement;
  }

  // Search for real-time information
  async searchRealTimeData(query) {
    try {
      console.log(`🔍 Searching for: "${query}"`);
      
      // Try multiple free search APIs
      let result = await this.tryDuckDuckGo(query);
      if (result) return result;
      
      // Fallback to Wikipedia API for factual queries
      result = await this.tryWikipedia(query);
      if (result) return result;
      
      // Last resort: generate helpful search suggestions
      return this.generateSearchSuggestions(query);
      
    } catch (error) {
      console.error('Search API error:', error.message);
      return this.generateSearchSuggestions(query);
    }
  }

  // DuckDuckGo Instant Answer API
  async tryDuckDuckGo(query) {
    try {
      const response = await axios.get(SEARCH_API_URL, {
        params: {
          q: query,
          format: 'json',
          no_redirect: '1',
          no_html: '1',
          skip_disambig: '1'
        },
        timeout: 5000
      });

      if (response.data) {
        if (response.data.AbstractText) {
          console.log('✅ Found DuckDuckGo abstract');
          return response.data.AbstractText;
        }
        if (response.data.Answer) {
          console.log('✅ Found DuckDuckGo answer');
          return response.data.Answer;
        }
        if (response.data.Definition) {
          console.log('✅ Found DuckDuckGo definition');
          return response.data.Definition;
        }
      }
      console.log('❌ No DuckDuckGo results');
      return null;
    } catch (error) {
      console.log('❌ DuckDuckGo failed:', error.message);
      return null;
    }
  }

  // Wikipedia API for factual information
  async tryWikipedia(query) {
    try {
      const response = await axios.get('https://en.wikipedia.org/api/rest_v1/page/summary/' + encodeURIComponent(query), {
        timeout: 5000,
        headers: {
          'User-Agent': 'TodoRevamp/1.0 (https://example.com/contact)'
        }
      });

      if (response.data && response.data.extract) {
        console.log('✅ Found Wikipedia summary');
        return response.data.extract;
      }
      console.log('❌ No Wikipedia results');
      return null;
    } catch (error) {
      console.log('❌ Wikipedia failed:', error.message);
      return null;
    }
  }

  // Generate helpful suggestions when search fails
  generateSearchSuggestions(query) {
    const lowerQuery = query.toLowerCase();
    
    if (lowerQuery.includes('weather')) {
      const location = this.extractLocation(query) || 'your location';
      return `Weather info needed for ${location}. 🌤️ Current weather varies by location. For accurate forecasts, check weather.com, your local weather app, or search "weather ${location}" online.`;
    }
    
    if (lowerQuery.includes('price') || lowerQuery.includes('cost')) {
      return `💰 Price information for: ${query}. Suggestion: Check online marketplaces, compare prices on shopping sites, or use price tracking tools like Google Shopping, Amazon, or PriceGrabber.`;
    }
    
    if (lowerQuery.includes('news') || lowerQuery.includes('latest')) {
      return `📰 Latest news about: ${query}. Suggestion: Check news websites like BBC, Reuters, AP News, or use news aggregators like Google News for recent updates.`;
    }
    
    if (lowerQuery.includes('stock') || lowerQuery.includes('bitcoin') || lowerQuery.includes('crypto')) {
      return `📈 Financial information for: ${query}. Suggestion: Check financial websites like Yahoo Finance, CoinMarketCap, or trading apps for current prices and market data.`;
    }
    
    if (lowerQuery.includes('time') || lowerQuery.includes('timezone')) {
      return `🕐 Time information for: ${query}. Current time varies by timezone. Check worldclock.com or search "time in [location]" for accurate time information.`;
    }
    
    if (lowerQuery.includes('traffic') || lowerQuery.includes('route')) {
      return `🚗 Traffic information for: ${query}. Check Google Maps, Waze, or local traffic apps for real-time traffic conditions and route planning.`;
    }
    
    return `🔍 Search suggestions for "${query}": Try being more specific with location, dates, or context. Check official websites, recent news sources, or specialized databases for accurate information.`;
  }

  // Helper function to extract location from query
  extractLocation(query) {
    const commonLocations = ['tokyo', 'london', 'new york', 'paris', 'sydney', 'toronto', 'dubai', 'singapore'];
    const found = commonLocations.find(loc => query.toLowerCase().includes(loc));
    return found ? found.charAt(0).toUpperCase() + found.slice(1) : null;
  }

  // Determine if search is needed based on text content
  needsRealTimeData(text) {
    const searchKeywords = [
      'current', 'latest', 'today', 'now', 'recent',
      'price', 'cost', 'weather', 'temperature',
      'news', 'update', 'status', 'stock',
      'exchange rate', 'currency', 'time zone'
    ];
    
    return searchKeywords.some(keyword => 
      text.toLowerCase().includes(keyword)
    );
  }

  // Main agent processing function
  async processNote(text) {
    let enhancedText = '';
    let searchData = '';

    // Check if we need real-time data
    if (this.needsRealTimeData(text)) {
      console.log('Searching for real-time data...');
      searchData = await this.searchRealTimeData(text);
      if (searchData) {
        enhancedText = `Real-time Info: ${searchData}\n\n`;
      }
    }

    // Enhance the original text
    const enhancement = await this.enhanceText(text);
    enhancedText += enhancement;

    return {
      original: text,
      enhanced: enhancedText,
      hasRealTimeData: !!searchData,
      searchResult: searchData || null,
      timestamp: new Date().toISOString()
    };
  }
}

// Initialize AI Agent
const aiAgent = new AIAgent();

// Routes

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ 
    status: 'healthy', 
    timestamp: new Date().toISOString(),
    service: 'AI Agent Backend'
  });
});

// Main AI agent endpoint
app.post('/agent-enhance', async (req, res) => {
  try {
    const { text } = req.body;
    
    if (!text || typeof text !== 'string' || text.trim().length === 0) {
      return res.status(400).json({ 
        error: 'Text is required and must be a non-empty string' 
      });
    }

    console.log(`Processing note: "${text}"`);
    
    const result = await aiAgent.processNote(text);
    
    res.json({
      success: true,
      data: result
    });
    
  } catch (error) {
    console.error('Error processing note:', error);
    res.status(500).json({ 
      error: 'Internal server error', 
      message: error.message 
    });
  }
});

// Simple text enhancement without search
app.post('/enhance-only', async (req, res) => {
  try {
    const { text } = req.body;
    
    if (!text || typeof text !== 'string' || text.trim().length === 0) {
      return res.status(400).json({ 
        error: 'Text is required and must be a non-empty string' 
      });
    }

    const enhanced = await aiAgent.enhanceText(text);
    
    res.json({
      success: true,
      original: text,
      enhanced: enhanced,
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('Error enhancing text:', error);
    res.status(500).json({ 
      error: 'Internal server error', 
      message: error.message 
    });
  }
});

// Search only endpoint
app.post('/search', async (req, res) => {
  try {
    const { query } = req.body;
    
    if (!query || typeof query !== 'string' || query.trim().length === 0) {
      return res.status(400).json({ 
        error: 'Query is required and must be a non-empty string' 
      });
    }

    const searchResult = await aiAgent.searchRealTimeData(query);
    
    res.json({
      success: true,
      query: query,
      result: searchResult,
      found: !!searchResult,
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('Error searching:', error);
    res.status(500).json({ 
      error: 'Internal server error', 
      message: error.message 
    });
  }
});

// Get agent configuration
app.get('/config', (req, res) => {
  res.json({
    model: 'microsoft/DialoGPT-medium',
    searchProvider: 'DuckDuckGo',
    features: [
      'Text Enhancement',
      'Real-time Search',
      'Subtask Generation',
      'Context-aware Processing'
    ],
    free: true,
    quotaLimits: 'None (using free APIs)'
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ 
    error: 'Something went wrong!', 
    message: err.message 
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({ 
    error: 'Endpoint not found',
    availableEndpoints: [
      'POST /agent-enhance',
      'POST /enhance-only', 
      'POST /search',
      'GET /health',
      'GET /config'
    ]
  });
});

// Start server
app.listen(port, () => {
  console.log(`🤖 AI Agent Backend running on http://localhost:${port}`);
  console.log(`📝 Endpoints available:`);
  console.log(`   POST /agent-enhance - Full AI enhancement with search`);
  console.log(`   POST /enhance-only - Text enhancement only`);
  console.log(`   POST /search - Search real-time data`);
  console.log(`   GET /health - Health check`);
  console.log(`   GET /config - Agent configuration`);
});

module.exports = app;