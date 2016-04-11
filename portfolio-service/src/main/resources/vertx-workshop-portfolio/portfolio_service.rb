require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.workshop.portfolio.PortfolioService
module VertxWorkshopPortfolio
  #  A service managing a portfolio.
  class PortfolioService
    # @private
    # @param j_del [::VertxWorkshopPortfolio::PortfolioService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWorkshopPortfolio::PortfolioService] the underlying java delegate
    def j_del
      @j_del
    end
    # @yield 
    # @return [void]
    def get_portfolio
      if block_given?
        return @j_del.java_method(:getPortfolio, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_portfolio()"
    end
    # @param [Fixnum] amount 
    # @param [Hash{String => Object}] quote 
    # @yield 
    # @return [void]
    def buy(amount=nil,quote=nil)
      if amount.class == Fixnum && quote.class == Hash && block_given?
        return @j_del.java_method(:buy, [Java::int.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(amount,::Vertx::Util::Utils.to_json_object(quote),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling buy(amount,quote)"
    end
    # @param [Fixnum] amount 
    # @param [Hash{String => Object}] quote 
    # @yield 
    # @return [void]
    def sell(amount=nil,quote=nil)
      if amount.class == Fixnum && quote.class == Hash && block_given?
        return @j_del.java_method(:sell, [Java::int.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(amount,::Vertx::Util::Utils.to_json_object(quote),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling sell(amount,quote)"
    end
    # @yield 
    # @return [void]
    def evaluate
      if block_given?
        return @j_del.java_method(:evaluate, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling evaluate()"
    end
    # @param [::Vertx::Vertx] vertx 
    # @return [::VertxWorkshopPortfolio::PortfolioService]
    def self.get_proxy(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWorkshopPortfolio::PortfolioService.java_method(:getProxy, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWorkshopPortfolio::PortfolioService)
      end
      raise ArgumentError, "Invalid arguments when calling get_proxy(vertx)"
    end
  end
end
