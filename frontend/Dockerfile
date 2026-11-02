# ==============================================================================
# Build Stage
# ==============================================================================
FROM node:22-alpine AS builder
WORKDIR /app

# Copy dependency specifications
COPY package*.json ./
RUN npm ci || npm install

# Build arguments for Vite environment variables
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}

# Copy frontend source code and compile production assets
COPY . .
RUN npm run build

# ==============================================================================
# Production Runtime Stage (Nginx)
# ==============================================================================
FROM nginx:alpine

# Copy custom Nginx configuration for React SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
